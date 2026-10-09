// Run as a function body through Blockbench MCP execute_script.
// Uses the actual model viewport; no display items are included.
if (Project.name !== 'funny_cat_stick_display_stand') throw Error('Select the display stand project');
Modes.options.edit.select();
const output = 'D:/modmoban/Funny_cat_stick/dev/models/funny_cat_stick_display_stand/preview_sheet.png';
const views = [
  ['FRONT / NORTH', [0, -2.5, -40]],
  ['LEFT / WEST', [-40, -2.5, 0]],
  ['RIGHT / EAST', [40, -2.5, 0]],
  ['BACK / SOUTH', [0, -2.5, 40]],
  ['45° FRONT', [32, 18, -32]],
  ['TOP / FOUR SLOTS', [0, 45, 0]],
];
const sheet = document.createElement('canvas');
sheet.width = 1800;
sheet.height = 1360;
const ctx = sheet.getContext('2d');
ctx.fillStyle = '#e9edf3';
ctx.fillRect(0, 0, sheet.width, sheet.height);
ctx.fillStyle = '#3b3240';
ctx.font = 'bold 32px sans-serif';
ctx.fillText('CAT WAND DISPLAY STAND', 30, 45);
ctx.fillStyle = '#766a76';
ctx.font = '17px sans-serif';
ctx.fillText('Minecraft Forge 1.20.1 · cherry-style wood · four empty display slots · actual Blockbench render', 30, 74);
const preview = MediaPreview;
for (let i = 0; i < views.length; i++) {
  const [label, position] = views[i];
  const x = i % 3 * 600;
  const y = Math.floor(i / 3) * 610 + 90;
  preview.setProjectionMode(true);
  preview.resize(580, 525);
  preview.controls.target.set(0, -2.5, 0);
  preview.camera.position.set(...position);
  preview.camera.up.set(...(label.startsWith('TOP') ? [0, 0, -1] : [0, 1, 0]));
  preview.camera.zoom = 525 / 40 / (label.startsWith('45') ? 15 : 12.5);
  preview.camera.lookAt(preview.controls.target);
  preview.camera.updateProjectionMatrix();
  Canvas.withoutGizmos(() => {
    preview.render();
    ctx.drawImage(preview.canvas, x + 10, y, 580, 525);
  });
  ctx.fillStyle = '#564653';
  ctx.font = 'bold 19px sans-serif';
  ctx.textAlign = 'center';
  ctx.fillText(label, x + 300, y + 555);
  ctx.textAlign = 'left';
}
ctx.fillStyle = '#766a76';
ctx.font = '16px sans-serif';
ctx.fillText('Footprint: 13×13/16 units · Height: 10.85/16 units · No cat wand geometry or textures baked in', 30, 1325);
Blockbench.writeFile(output, {content: sheet.toDataURL('image/png'), savetype: 'image'});
return {file: output, views: views.map(v => v[0])};
