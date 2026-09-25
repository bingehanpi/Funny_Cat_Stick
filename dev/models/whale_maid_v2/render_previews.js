// Function body for Blockbench MCP execute_script. Actual viewport renders, not AI concept art.
if (Project.name !== 'whale_maid_v2') throw Error('Select whale_maid_v2');
const output = 'D:/modmoban/Funny_cat_stick/dev/models/whale_maid_v2/';
const p = MediaPreview;
function shot(ctx, x, y, w, h, camera) {
  p.setProjectionMode(true); p.resize(w, h); p.camera.zoom = h / 40 / (camera[1] > 16 ? 40 : 36.8);
  p.controls.target.set(0, 16, .7); p.camera.position.set(...camera);
  p.camera.lookAt(p.controls.target); p.camera.updateProjectionMatrix();
  Canvas.withoutGizmos(() => {p.render();ctx.drawImage(p.canvas, x, y, w, h);});
}
function sheet(w, h, title, subtitle) {
  const c = document.createElement('canvas'); c.width = w; c.height = h;
  const g = c.getContext('2d'); g.fillStyle = '#e7edf9'; g.fillRect(0, 0, w, h);
  g.fillStyle = '#334672'; g.font = 'bold 30px sans-serif'; g.fillText(title, 32, 45);
  g.fillStyle = '#65769b'; g.font = '17px sans-serif'; g.fillText(subtitle, 32, 75);
  return [c, g];
}
Modes.options.edit.select(); Animator.showDefaultPose();
let [c, g] = sheet(1840, 900, 'WHALE MAID / V2', 'New independent model | '+Cube.all.length+' cubes / '+Group.all.length+' bones | 1024px atlas | Original model preserved');
const views = [['FRONT',[0,16,-80]],['LEFT',[-80,16,0]],['BACK',[0,16,80]],['3/4',[-32,23,-80]]];
for (let i = 0; i < views.length; i++) {
  const x = i * 460;
  shot(g, x, 92, 460, 758, views[i][1]);
  g.fillStyle = '#4a5f8b';g.font = 'bold 20px sans-serif';g.textAlign = 'center';g.fillText(views[i][0], x+230, 881);g.textAlign = 'left';
}
Blockbench.writeFile(output + 'preview_sheet.png', {content:c.toDataURL('image/png'),savetype:'image'});
const files = ['preview_sheet.png'];
for (const animationName of ['idle','walk']) {
  const a = Animation.all.find(a=>a.name.endsWith('.'+animationName));
  Modes.options.animate.select(); a.select();
  [c,g] = sheet(1440, 1280, 'WHALE MAID / '+animationName.toUpperCase(), 'Actual Blockbench poses | Top: 3/4 view / Bottom: side view | Loop: '+a.length+' seconds');
  for(let i=0;i<4;i++) {
    const t=animationName==='idle'?[0,1,2.825,3][i]:[3.3,3.366667,3.425,3.6][i]; Timeline.setTime(t);Animator.preview();
    shot(g,i*360,105,360,550,[-32,22,-80]);shot(g,i*360,700,360,550,[-80,16,0]);
    g.fillStyle='#4a5f8b';g.font='bold 19px sans-serif';g.textAlign='center';g.fillText('t = '+t.toFixed(2)+' s',i*360+180,680);g.textAlign='left';
  }
  const file=animationName+'_poses.png';files.push(file);
  Blockbench.writeFile(output+file,{content:c.toDataURL('image/png'),savetype:'image'});
}
const blink=Animation.all.find(a=>a.name.endsWith('.blink'));
if(blink) {
  Modes.options.animate.select();blink.select();
  [c,g]=sheet(1600,620,'WHALE MAID / BLINK','0.30-second blink | Also included in idle | Actual model renders');
  const times=[0,.067,.125,.24],labels=['OPEN','CLOSING','CLOSED','OPENING'];
  for(let i=0;i<4;i++) {
    Timeline.setTime(times[i]);Animator.preview();
    p.setProjectionMode(true);p.resize(400,480);p.camera.zoom=480/40/16.5;
    p.controls.target.set(0,24.2,0);p.camera.position.set(0,24.2,-80);
    p.camera.lookAt(p.controls.target);p.camera.updateProjectionMatrix();
    Canvas.withoutGizmos(()=>{p.render();g.drawImage(p.canvas,i*400,95);});
    g.fillStyle='#334672';g.font='bold 22px sans-serif';g.textAlign='center';g.fillText(labels[i],i*400+200,600);g.textAlign='left';
  }
  Blockbench.writeFile(output+'blink_poses.png',{content:c.toDataURL('image/png'),savetype:'image'});files.push('blink_poses.png');
}
Modes.options.edit.select();Animator.showDefaultPose();
return {files:files.map(f=>output+f)};
