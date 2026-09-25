// Run as a function body through Blockbench MCP execute_script.
// Repaints only the V2 project; deliberately refuses other projects.
if (Project.name !== 'whale_maid_v2') throw Error('Select whale_maid_v2 first');
const tex = Texture.all.find(t => t.name === 'whale_maid_v2.png');
if (!tex) throw Error('Create the 1024px whale_maid_v2.png texture first');
Undo.initEdit({elements: Cube.all.slice(), textures: [tex], bitmap: true});
const rects = [];
for (const c of Cube.all) {
  if (c.name.startsWith('blink_lid_')) continue;
  c.box_uv = false;
  const size = c.size();
  for (const [dir, face] of Object.entries(c.faces)) {
    const pp = dir === 'north' && c.name === 'skin_face' ? 12 : dir === 'north' && c.name === 'lace_apron_main' ? 12 : 6;
    const axes = /up|down/.test(dir) ? [0, 2] : /east|west/.test(dir) ? [2, 1] : [0, 1];
    rects.push({c, dir, face, w: Math.max(2, Math.ceil(size[axes[0]] * pp)), h: Math.max(2, Math.ceil(size[axes[1]] * pp))});
  }
}
rects.sort((a, b) => b.h - a.h);
let x = 2, y = 2, row = 0;
for (const r of rects) {
  if (x + r.w + 2 > 1024) {x = 2; y += row + 3; row = 0;}
  r.x = x; r.y = y;
  r.face.uv = [x, y, x + r.w, y + r.h]; r.face.texture = tex.uuid; r.face.rotation = 0;
  x += r.w + 3; row = Math.max(row, r.h);
}
if (y + row > 1024) throw Error('Atlas overflow: ' + (y + row));
Project.box_uv = false;
Project.texture_width = Project.texture_height = 1024;
const canvas = document.createElement('canvas'); canvas.width = canvas.height = 1024;
const ctx = canvas.getContext('2d'); ctx.imageSmoothingEnabled = false;
const rgb = hex => hex.match(/\w\w/g).map(s => parseInt(s, 16));
const shade = (hex, factor) => '#' + rgb(hex.replace('#', '')).map(v => Math.max(0, Math.min(255, Math.round(v * factor))).toString(16).padStart(2, '0')).join('');
const mix = (a, b, t) => '#' + rgb(a.replace('#', '')).map((v, i) => Math.round(v * (1 - t) + rgb(b.replace('#', ''))[i] * t).toString(16).padStart(2, '0')).join('');
for (const r of rects) {
  const n = r.c.name, w = r.w, h = r.h;
  let base = /^skin/.test(n) ? '#ffe4dc' : /^lace|^stocking/.test(n) ? '#f8f1f6' : /^gold/.test(n) ? '#caa16d' : /^gem|^ribbon_blue/.test(n) ? '#55a1d7' : /^fin_dark|^headband/.test(n) ? '#29386b' : /^fin_/.test(n) ? '#4378b6' : /^dress/.test(n) ? '#283359' : /^shoe/.test(n) ? '#202b4c' : '#4b60a0';
  const factor = {north: 1, south: .98, east: .91, west: .94, up: 1.06, down: .80}[r.dir];
  ctx.save(); ctx.beginPath(); ctx.rect(r.x, r.y, w, h); ctx.clip(); ctx.translate(r.x, r.y);
  for (let py = 0; py < h; py++) {
    const frac = py / Math.max(1, h - 1);
    const wy = /up/.test(r.dir) ? r.c.to[1] : /down/.test(r.dir) ? r.c.from[1] : r.c.to[1] - frac * (r.c.to[1] - r.c.from[1]);
    let color = base;
    if (/^hair/.test(n)) {
      color = mix('#455792', '#6a9bd4', Math.round(Math.max(0, Math.min(1, (24 - wy) / 19)) * 12) / 12);
      if (/^hair_bang_\d+$/.test(n)) color = shade(color, [1.02,.97,1.04,1.0,.98,1.025,.99][Number(n.split('_').pop())]);
    }
    const ramp = /^hair/.test(n) ? 1 : 1.025 - Math.floor(frac * 6) * .009;
    ctx.fillStyle = shade(color, factor * ramp); ctx.fillRect(0, py, w, 1);
  }
  const R = (a, b, ww, hh, color) => {ctx.fillStyle = color;ctx.fillRect(Math.round(a * w / 100), Math.round(b * h / 100), Math.max(1, Math.round(ww * w / 100)), Math.max(1, Math.round(hh * h / 100)));};
  const P = (pts, color) => {ctx.fillStyle = color;ctx.beginPath();pts.forEach(([a,b],i)=>{const xx=Math.round(a*w/100),yy=Math.round(b*h/100);i?ctx.lineTo(xx,yy):ctx.moveTo(xx,yy);});ctx.closePath();ctx.fill();};
  if (/^hair/.test(n) && !/back_lock|back_point|round_scalp/.test(n) && /north|south/.test(r.dir) && h > 12 && w > 7) {
    const seed = [...n].reduce((s,c)=>s+c.charCodeAt(0),0);
    const bright = /^hair_light/.test(n) ? '#77a4d8' : '#6378b4';
    if (n.includes('bang') || n.includes('scalp')) {
      P([[17,26],[36,21],[42,28],[69,30],[74,39],[43,42],[36,35],[19,35]],bright);
      if (w > 16) R(77,26,9,10,'#7086bf');
      if (n.includes('bang_')) R(0,45,6,48,'#3d508b');
    } else {
      P([[26,8],[42,6],[39,31],[48,55],[43,83],[31,91],[36,59],[28,34]],shade(bright,.97));
      if (seed % 3 === 0) P([[66,20],[72,18],[77,53],[72,80],[66,83],[69,49]],shade(bright,1.02));
    }
  }
  if (/^lace/.test(n) && /north|south|east|west/.test(r.dir) && !/apron_main|bib|strap|waistband/.test(n)) {
    R(0,78,100,16,'#dfd7e8');
    for(let xx=10;xx<95;xx+=30){P([[xx,31],[xx+7,19],[xx+15,33],[xx+7,46]],'#e5dbea');R(xx+3,27,6,10,'#fffbfd');}
  }
  if (/^dress_skirt_panel/.test(n) && /north|south/.test(r.dir)) {
    R(0,82,100,3,'#8e795f'); R(0,93,100,3,'#c7a373');
    P([[3,85],[15,79],[28,81],[41,86],[54,83],[66,79],[80,83],[95,85],[95,88],[80,86],[66,82],[54,86],[41,89],[28,84],[15,82],[3,88]],'#caa574');
    R(27,75,5,4,'#dcbc87');R(70,75,5,4,'#dcbc87');
    R(4,9,7,53,'#303f68');R(87,8,5,58,'#242f52');
  }
  if (n === 'dress_bib_insert' && r.dir === 'north') {
    for (const xx of [27,68]) for (const yy of [24,51,78]) R(xx,yy,8,7,'#d1ad76');
  }
  if (/gold_sleeve_band/.test(n)) for(let xx=9;xx<96;xx+=22)R(xx,5,8,90,'#e3c396');
  if (/^ribbon_blue/.test(n)) {R(12,15,17,52,'#73b8e6');R(73,21,12,63,'#3c87c2');}
  if (/back_lock|back_point|round_scalp/.test(n) && /north|south/.test(r.dir)) {
    for(let py=0;py<h;py++) {
      const wy=r.c.to[1]-py/h*(r.c.to[1]-r.c.from[1]);
      const xx=Math.round(w*(.32+.1*Math.sin(wy*.47)));
      const local=mix('#455792','#6a9bd4',Math.max(0,Math.min(1,(24-wy)/19)));
      ctx.fillStyle=shade(local,factor*1.075);
      ctx.fillRect(xx,py,Math.max(1,Math.round(w*.12)),1);
    }
  }
  if (/^fin_fluke/.test(n) && /north|south/.test(r.dir)) {
    for(let py=0;py<h;py++) for(let px=0;px<w;px++) {
      const wx=r.c.from[0]+px/w*(r.c.to[0]-r.c.from[0]);
      const wy=r.c.to[1]-py/h*(r.c.to[1]-r.c.from[1]);
      const ray=n.includes('upright')?[-.1,-.33,-.58]:[-1.2,-1.8,-2.5];
      const light=ray.some(slope=>Math.abs(wx-(-7.12+slope*(wy-6.4)))<.10);
      const local=mix('#365990','#6598cd',Math.max(0,Math.min(1,(wy-5.8)/4.7)));
      ctx.fillStyle=light?'#81b4df':shade(local,factor);
      ctx.fillRect(px,py,1,1);
    }
  }
  if (n === 'skin_face' && r.dir === 'north') {
    R(0,0,100,100,'#ffe5dd');
    for(const ex of [12,64]) {
      P([[ex,53],[ex+3,49],[ex+22,49],[ex+24,55],[ex+23,82],[ex+18,87],[ex+5,86],[ex,80]],'#604653');
      R(ex+2,54,21,25,'#fff8f5');R(ex+5,77,16,8,'#fff8f5');
      R(ex+7,52,14,31,'#293f76');R(ex+8,62,13,20,'#3d65a1');R(ex+9,73,12,11,'#568fbe');R(ex+11,80,9,5,'#80b8d6');
      R(ex+12,55,6,14,'#26365e');R(ex+9,53,6,8,'#e8f4ff');R(ex+18,71,3,5,'#c3e2f5');
      P([[ex-2,55],[ex-4,47],[ex+5,49],[ex+9,46],[ex+22,48],[ex+25,53],[ex+23,56],[ex+17,53],[ex+3,53]],'#2b2534');
    }
    R(7,84,14,5,'#f5bec2');R(79,84,14,5,'#f5bec2');R(10,86,8,2,'#efaeb5');R(82,86,8,2,'#efaeb5');R(48,94,4,1.7,'#b16d79');
  }
  if (n === 'lace_apron_main' && r.dir === 'north') {
    R(0,0,100,100,'#fcf5fa');R(3,6,3,85,'#ded4e5');R(94,6,3,85,'#ded4e5');R(8,91,84,3,'#dfd3e5');
    for(let xx=12;xx<92;xx+=13){R(xx,86,4,3,'#e3d7e8');R(xx,9,4,3,'#e9dfec');}
    P([[19,61],[23,48],[35,42],[51,42],[65,52],[68,59],[76,58],[77,46],[87,40],[88,51],[85,61],[77,70],[68,76],[39,78],[26,73]],'#5379b9');
    P([[22,64],[32,61],[44,65],[51,74],[40,77],[30,73]],'#87b5dc');
    R(25,58,4,4,'#f6f7fb');R(58,56,4,4,'#24395e');
    P([[48,64],[58,65],[62,73],[56,75],[50,71]],'#3e639f');
    R(45,29,5,9,'#739bcc');R(37,25,5,5,'#739bcc');R(52,23,5,5,'#739bcc');
  }
  ctx.restore();
}
// Dedicated lid patches must not overwrite the main atlas or the original eyes.
const lids = Cube.all.filter(c => c.name.startsWith('blink_lid_'));
if (lids.length && y + row >= 750) throw Error('Main atlas overlaps reserved blink patches');
for (const c of lids) {
  const tx = c.name.endsWith('right') ? 20 : 84, ty = 760, w = 48, h = 48;
  ctx.fillStyle = '#ffe5dd'; ctx.fillRect(tx-2, ty-2, w+4, h+4);
  ctx.fillStyle = '#2b2534';
  ctx.fillRect(tx+8,ty+41,31,3);ctx.fillRect(tx+4,ty+39,7,3);ctx.fillRect(tx+37,ty+39,6,3);
  ctx.fillStyle = '#674652';ctx.fillRect(tx+11,ty+44,23,1);
  for (const [dir,f] of Object.entries(c.faces)) {
    f.texture=tex.uuid; f.uv=dir==='north'?[tx,ty,tx+w,ty+h]:[tx,ty,tx+2,ty+2];
  }
}
tex.fromDataURL(canvas.toDataURL('image/png'));
Undo.finishEdit('Pack and paint whale maid V2 texture atlas');
Canvas.updateAll();
return {texture:tex.name,faces:rects.length,used_height:y+row,resolution:1024};
