// Function body for Blockbench MCP execute_script. Renders the actual V2 model.
if (Project.name !== 'whale_maid_v2') throw Error('Select whale_maid_v2');
const output = 'D:/modmoban/Funny_cat_stick/dev/models/whale_maid_v2/iron_pot_preview.png';
const views = [
  ['FRONT', [0, 16, -80], [0, 27, -80]],
  ['LEFT', [-80, 16, 0], [-80, 27, 0]],
  ['RIGHT', [80, 16, 0], [80, 27, 0]],
  ['BACK', [0, 16, 80], [0, 27, 80]],
  ['45° FRONT', [-40, 23, -70], [-40, 29, -70]],
];
Modes.options.edit.select();
Animator.showDefaultPose();
const canvas = document.createElement('canvas');
canvas.width = 2200;
canvas.height = 1300;
const ctx = canvas.getContext('2d');
ctx.fillStyle = '#e8edf5';
ctx.fillRect(0, 0, canvas.width, canvas.height);
ctx.fillStyle = '#263143';
ctx.font = 'bold 36px sans-serif';
ctx.fillText('WHALE MAID V2  /  IRON POT', 35, 51);
ctx.font = '18px sans-serif';
ctx.fillStyle = '#526075';
ctx.fillText('Actual Blockbench model · 5 angles · full body + head close-up · head → iron_pot', 35, 82);
const preview = MediaPreview;
function shot(x, y, width, height, position, target, close) {
  preview.setProjectionMode(true);
  preview.resize(width, height);
  preview.controls.target.set(0, target[1], 0);
  preview.camera.position.set(...position);
  preview.camera.zoom = height / 40 / (close ? 14 : 36.8);
  preview.camera.lookAt(preview.controls.target);
  preview.camera.updateProjectionMatrix();
  Canvas.withoutGizmos(() => {
    preview.render();
    ctx.drawImage(preview.canvas, x, y, width, height);
  });
}
for (let i = 0; i < views.length; i++) {
  const [label, full, head] = views[i];
  const x = i * 440;
  shot(x, 96, 440, 668, full, [0, 16, 0], false);
  shot(x, 803, 440, 427, head, [0, 27, 0], true);
  ctx.fillStyle = '#344258';
  ctx.font = 'bold 20px sans-serif';
  ctx.textAlign = 'center';
  ctx.fillText(label, x + 220, 791);
  ctx.textAlign = 'left';
}
ctx.fillStyle = '#526075';
ctx.font = '16px sans-serif';
ctx.fillText('Iron pot moved down 1 model unit to sit closer to the hair. Original model, UVs and animations retained.', 30, 1270);
Blockbench.writeFile(output, {content: canvas.toDataURL('image/png'), savetype: 'image'});
return {file: output, size: [canvas.width, canvas.height], views: views.map(v => v[0])};
