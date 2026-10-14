import { readFileSync, writeFileSync } from 'node:fs';
import { deflateSync } from 'node:zlib';

const assets = new URL('../src/main/resources/assets/lightsabers/', import.meta.url);
const width = 64;
const height = 128;
const frames = 40;
const colors = {
  lightsaber: [55, 235, 255],
  blue_lightsaber: [65, 120, 255],
  green_lightsaber: [75, 255, 105],
  red_lightsaber: [255, 45, 30],
  purple_lightsaber: [190, 75, 255],
  yellow_lightsaber: [255, 225, 55],
};

function chunk(type, data) {
  const body = Buffer.concat([Buffer.from(type), data]);
  let crc = 0xffffffff;
  for (const byte of body) {
    crc ^= byte;
    for (let bit = 0; bit < 8; bit++) crc = (crc >>> 1) ^ ((crc & 1) ? 0xedb88320 : 0);
  }
  const length = Buffer.alloc(4);
  length.writeUInt32BE(data.length);
  const checksum = Buffer.alloc(4);
  checksum.writeUInt32BE((crc ^ 0xffffffff) >>> 0);
  return Buffer.concat([length, body, checksum]);
}

function smoothstep(value) {
  const t = Math.max(0, Math.min(1, value));
  return t * t * (3 - 2 * t);
}

function auraPng(rgb) {
  const rowBytes = width * 4 + 1;
  const pixels = Buffer.alloc(rowBytes * height * frames);
  for (let frame = 0; frame < frames; frame++) {
    const phase = frame / frames * 2 * Math.PI;
    const pulse = (1 - Math.cos(phase)) / 2;
    const radius = 0.9 + pulse * 0.18;
    const intensity = 0.38 + pulse * 0.18 + Math.sin(phase * 7) * 0.012;
    for (let y = 0; y < height; y++) {
      const endFade = smoothstep(y / (height * 0.075)) * smoothstep((height - 1 - y) / (height * 0.075));
      for (let x = 0; x < width; x++) {
        const distance = (2 * x / (width - 1) - 1) / radius;
        const edgeFade = smoothstep((1 - Math.abs(2 * x / (width - 1) - 1)) * 8);
        const alpha = Math.round(255 * intensity * Math.exp(-4.5 * distance * distance) * edgeFade * endFade);
        const offset = (frame * height + y) * rowBytes + 1 + x * 4;
        pixels.set([...rgb, alpha], offset);
      }
    }
  }
  const header = Buffer.alloc(13);
  header.writeUInt32BE(width, 0);
  header.writeUInt32BE(height * frames, 4);
  header[8] = 8;
  header[9] = 6;
  return Buffer.concat([
    Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]),
    chunk('IHDR', header), chunk('IDAT', deflateSync(pixels, { level: 9 })), chunk('IEND', Buffer.alloc(0)),
  ]);
}

const faces = names => Object.fromEntries(names.map(name => [name, { uv: [0, 0, 16, 16], texture: '#aura' }]));
const plane = (from, to, directions, angle) => ({
  from, to, shade: false, light_emission: 15,
  ...(angle === undefined ? {} : { rotation: { origin: [8, 8, 8], axis: 'y', angle } }),
  faces: faces(directions),
});
const saveJson = (path, value) => writeFileSync(new URL(path, assets), JSON.stringify(value, null, 2) + '\n');

for (const [name, rgb] of Object.entries(colors)) {
  const baseModel = JSON.parse(readFileSync(new URL(`models/item/${name}.json`, assets), 'utf8'));
  writeFileSync(new URL(`textures/item/${name}_aura.png`, assets), auraPng(rgb));
  saveJson(`textures/item/${name}_aura.png.mcmeta`, {
    animation: { width, height, frametime: 1, interpolate: true },
  });
  // Crossed, double-sided planes give a soft halo from any viewing angle, without covering the hilt.
  saveJson(`models/item/${name}_aura.json`, {
    ambientocclusion: false,
    textures: { aura: `lightsabers:item/${name}_aura`, particle: baseModel.textures.particle },
    elements: [
      plane([4.5, 6.8, 8], [11.5, 23.1, 8], ['north', 'south']),
      plane([8, 6.8, 4.5], [8, 23.1, 11.5], ['east', 'west']),
      plane([4.5, 6.8, 8], [11.5, 23.1, 8], ['north', 'south'], 45),
      plane([4.5, 6.8, 8], [11.5, 23.1, 8], ['north', 'south'], -45),
    ],
    display: baseModel.display,
  });
  saveJson(`items/${name}.json`, {
    model: {
      type: 'minecraft:composite',
      models: [
        { type: 'minecraft:model', model: `lightsabers:item/${name}` },
        { type: 'minecraft:model', model: `lightsabers:item/${name}_aura` },
      ],
    },
  });
}
console.log(`Generated ${Object.keys(colors).length} lightsaber auras (${frames}-tick seamless pulse).`);
