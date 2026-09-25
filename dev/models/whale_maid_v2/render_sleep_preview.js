// Function body for Blockbench MCP execute_script.
// Show local animation only; bed rotation/translation belongs to the game renderer.
if(Project.name!=='whale_maid_v2')throw Error('Select whale_maid_v2');
Timeline.pause();Modes.options.animate.select();
const animation=Animation.all.find(a=>a.name==='animation.whale_maid_v2.sleep');
if(!animation)throw Error('Missing sleep animation');
animation.select();
const canvas=document.createElement('canvas');canvas.width=1440;canvas.height=850;
const ctx=canvas.getContext('2d');ctx.fillStyle='#e7edf9';ctx.fillRect(0,0,1440,850);
ctx.fillStyle='#334672';ctx.font='bold 30px sans-serif';ctx.fillText('WHALE MAID / SLEEP - LOCAL POSE',30,43);
ctx.font='18px sans-serif';ctx.fillText('Root rotation [0,0,0] / Root position [0,0,0] / Eyes remain closed',30,76);
const p=MediaPreview;
function shot(x,w,h,camera,target,span,time,label) {
  Timeline.setTime(time);Animator.preview();
  p.setProjectionMode(true);p.resize(w,h);p.camera.zoom=h/40/span;
  p.controls.target.set(...target);p.camera.position.set(...camera);
  p.camera.lookAt(p.controls.target);p.camera.updateProjectionMatrix();
  Canvas.withoutGizmos(()=>{p.render();ctx.drawImage(p.canvas,x,100,w,h);});
  ctx.fillStyle='#4a5f8b';ctx.font='bold 19px sans-serif';ctx.textAlign='center';
  ctx.fillText(label,x+w/2,100+h+25);ctx.textAlign='left';
}
shot(0,480,655,[0,16,-80],[0,16,0],36.8,0,'EXHALE / t = 0.0 s');
shot(480,480,655,[-24,22,-80],[0,16,0],40,2,'INHALE / t = 2.0 s');
shot(960,480,655,[0,24,-80],[0,24,0],17,2,'CLOSED EYES');
ctx.fillStyle='#65769b';ctx.font='17px sans-serif';
ctx.fillText('Upright in Blockbench by design. The game renderer applies the bed transform.',30,825);
Blockbench.writeFile('D:/modmoban/Funny_cat_stick/dev/models/whale_maid_v2/sleep_preview.png',{content:canvas.toDataURL('image/png'),savetype:'image'});
Timeline.setTime(0);Animator.preview();
return{path:'D:/modmoban/Funny_cat_stick/dev/models/whale_maid_v2/sleep_preview.png',renderer_transform_applied:false};
