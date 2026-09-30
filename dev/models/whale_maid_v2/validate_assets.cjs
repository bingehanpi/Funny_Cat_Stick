// Read-only consistency checks. Run: node dev/models/whale_maid_v2/validate_assets.cjs
const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const crypto = require('node:crypto');
const root = path.resolve(__dirname, '../../..');
const assets = path.join(root, 'src/main/resources/assets/funny_cat_stick');
const read = p => JSON.parse(fs.readFileSync(p, 'utf8').replace(/^\uFEFF/, ''));
const source = read(path.join(__dirname, 'whale_maid_v2.bbmodel'));
const geo = read(path.join(assets, 'geo/whale_maid_v2.geo.json'));
const anim = read(path.join(assets, 'animations/whale_maid_v2.animation.json'));
const png = fs.readFileSync(path.join(assets, 'textures/entity/whale_maid_v2.png'));
assert.equal(geo.format_version, '1.12.0');
assert.equal(anim.format_version, '1.8.0');
const model = geo['minecraft:geometry'][0];
assert.equal(model.description.identifier, 'geometry.whale_maid_v2');
assert.equal(png.toString('hex', 0, 8), '89504e470d0a1a0a');
assert.equal(png.readUInt32BE(16), 1024); assert.equal(png.readUInt32BE(20), 1024);
assert.equal(model.description.texture_width, 1024); assert.equal(model.description.texture_height, 1024);
const names = new Set(model.bones.map(b => b.name));
assert.equal(names.size, model.bones.length);
assert(names.has('root') && names.has('head') && names.has('tail_flukes'));
let cubes = 0, faces = 0;
for (const b of model.bones) {
  assert(!b.parent || names.has(b.parent), 'Missing parent: '+b.name);
  for (const c of b.cubes || []) {
    cubes++; assert(c.size.every(v => v > 0));
    for (const f of Object.values(c.uv)) {
      faces++;
      for (let i = 0; i < 2; i++) {
        const end = f.uv[i] + f.uv_size[i];
        assert(f.uv[i] >= 0 && f.uv[i] <= 1024 && end >= 0 && end <= 1024, 'UV out of bounds');
        assert.notEqual(f.uv_size[i], 0);
      }
    }
  }
}
assert.equal(cubes, source.elements.length); assert.equal(cubes, 559); assert.equal(names.size, 40);
const pot = model.bones.find(b => b.name === 'iron_pot');
assert.equal(pot.parent, 'head');
assert.equal(pot.cubes.length, 11);
assert(names.has('eyelid_left') && names.has('eyelid_right'));
const texture = source.textures.find(t => t.name === 'whale_maid_v2.png');
assert(texture.source.startsWith('data:image/png;base64,'));
assert(png.equals(Buffer.from(texture.source.split(',')[1], 'base64')), 'PNG differs from source texture');
const timings = {'animation.whale_maid_v2.idle':4, 'animation.whale_maid_v2.walk':4.8, 'animation.whale_maid_v2.blink':.3, 'animation.whale_maid_v2.sleep':4};
assert.deepEqual(Object.keys(anim.animations).sort(), Object.keys(timings).sort());
let keys = 0;
for (const [name, a] of Object.entries(anim.animations)) {
  assert.equal(a.loop === true, !name.endsWith('.blink')); assert.equal(a.animation_length, timings[name]);
  for (const [bone, channels] of Object.entries(a.bones)) {
    assert(names.has(bone));
    for (const channel of Object.values(channels)) {
      const entries = Object.entries(channel).sort((a,b)=>Number(a[0])-Number(b[0]));
      assert.equal(Number(entries[0][0]), 0); assert.equal(Number(entries.at(-1)[0]), a.animation_length);
      const val = v => Array.isArray(v) ? v : v.post;
      assert.deepEqual(val(entries[0][1]), val(entries.at(-1)[1]), 'Open loop: '+name+'/'+bone);
      for (const [t,v] of entries) {
        keys++; assert(Number(t)>=0 && Number(t)<=a.animation_length);
        assert.equal(val(v).length,3); assert(val(v).every(Number.isFinite));
        assert(!v.lerp_mode || ['linear','catmullrom'].includes(v.lerp_mode));
      }
    }
  }
}
for (const a of source.animations) {
  assert.equal(a.snapping, 120);
  for (const b of Object.values(a.animators)) {
    const seen = new Set();
    for (const k of b.keyframes || []) {
      const key = k.channel+':'+k.time; assert(!seen.has(key), 'Duplicate source key: '+key); seen.add(key);
    }
  }
}
const before = read(path.join(__dirname, 'backups/before_arm_blink/whale_maid_v2.bbmodel'));
for (const previous of before.animations) {
  const current = source.animations.find(a=>a.name===previous.name);
  for (const [id,bone] of Object.entries(previous.animators)) {
    const actual=current.animators[id].keyframes;
    if(previous.name.endsWith('.walk')) {
      assert.deepEqual(actual.filter(k=>k.time<=1.2), bone.keyframes, 'Original gait keys altered: '+id);
      assert.equal(actual.length,bone.keyframes.length+3*bone.keyframes.filter(k=>k.time>1e-5).length);
      for(let repeat=1;repeat<4;repeat++)for(const k of bone.keyframes.filter(k=>k.time>1e-5)) {
        const copy=actual.find(v=>v.channel===k.channel && Math.abs(v.time-k.time-repeat*1.2)<1e-5);
        assert(copy,'Missing repeated key: '+id);
        assert.equal(copy.interpolation,k.interpolation);
        assert.deepEqual(copy.data_points,k.data_points,'Repeated gait value changed: '+id);
      }
    } else assert.deepEqual(actual, bone.keyframes, 'Existing animation altered: '+previous.name+'/'+id);
  }
}
const beforeWalk=read(path.join(__dirname,'backups/before_walk_blink/whale_maid_v2.bbmodel'));
assert.deepEqual(source.elements.slice(0, beforeWalk.elements.length),beforeWalk.elements,'Original geometry changed');
for(const previous of beforeWalk.animations.filter(a=>!a.name.endsWith('.walk'))) {
  const current=source.animations.find(a=>a.name===previous.name);
  assert.equal(current.length,previous.length);assert.equal(current.loop,previous.loop);
  for(const [id,bone]of Object.entries(previous.animators))assert.deepEqual(current.animators[id].keyframes,bone.keyframes);
}
const walk=anim.animations['animation.whale_maid_v2.walk'];
for(const side of['left','right']) {
  const channel=walk.bones['eyelid_'+side].position;
  const entries=Object.entries(channel);
  const closed=entries.filter(([,v])=>(Array.isArray(v)?v:v.post)[1]===-3.65);
  assert.equal(closed.length,2);assert(Math.abs(Number(closed[0][0])-3.4)<1e-5);
  assert(Math.abs(Number(closed[1][0])-3.45)<1e-5);
}
for (const old of before.elements) {
  const current = source.elements.find(c=>c.uuid===old.uuid);
  assert(current, 'Original V2 cube removed: '+old.name);
  const arm = /^(dress_(shoulder_puff|upper_sleeve|lower_sleeve|elbow_fold)|gold_sleeve_band_|lace_cuff_|skin_hand_|skin_thumb_)/.test(old.name);
  if (arm) {
    const factor = /^skin_(hand|thumb)_/.test(old.name) ? 1.06 : 1.1;
    for (const axis of [0,2]) assert(Math.abs((current.to[axis]-current.from[axis])/(old.to[axis]-old.from[axis])-factor)<1e-6);
    assert.equal(current.from[1],old.from[1]); assert.equal(current.to[1],old.to[1]);
  } else {
    assert.deepEqual(current.from,old.from); assert.deepEqual(current.to,old.to);
  }
}
const blink = anim.animations['animation.whale_maid_v2.blink'];
assert.deepEqual(Object.keys(blink.bones).sort(),['eyelid_left','eyelid_right']);
for (const bone of Object.values(blink.bones)) {
  const values=Object.values(bone.position).map(v=>Array.isArray(v)?v:v.post);
  assert(values.some(v=>v[1]===-3.65 && v[2]===-.3), 'Missing fully closed eyelid pose');
}
const beforeSleep=read(path.join(__dirname,'backups/before_sleep/whale_maid_v2.bbmodel'));
const beforeSleepExport=read(path.join(__dirname,'backups/before_sleep/whale_maid_v2.animation.json'));
assert.deepEqual(source.elements.slice(0, beforeSleep.elements.length),beforeSleep.elements,'Original geometry changed');
for(const previous of beforeSleep.animations) {
  const current=source.animations.find(a=>a.name===previous.name);
  assert.equal(current.length,previous.length);assert.equal(current.loop,previous.loop);
  for(const [id,bone]of Object.entries(previous.animators))assert.deepEqual(current.animators[id].keyframes,bone.keyframes);
  assert.deepEqual(anim.animations[previous.name],beforeSleepExport.animations[previous.name],'Sleep edit changed old exported animation');
}
const sleep=anim.animations['animation.whale_maid_v2.sleep'];
const vector=v=>Array.isArray(v)?v:v.post;
for(const side of['left','right'])for(const v of Object.values(sleep.bones['eyelid_'+side].position)) {
  assert.deepEqual(vector(v),[0,-3.65,-.3],'Sleep must keep both eyes closed');
}
for(const v of Object.values(sleep.bones.root.rotation))assert.deepEqual(vector(v),[0,0,0]);
for(const v of Object.values(sleep.bones.root.position))assert.deepEqual(vector(v),[0,0,0]);
assert(Object.values(sleep.bones.chest.scale).some(v=>vector(v)[2]>1.02),'Missing breathing motion');
const beforeRoot=read(path.join(__dirname,'backups/before_sleep_root_identity/whale_maid_v2.bbmodel'));
const beforeRootExport=read(path.join(__dirname,'backups/before_sleep_root_identity/whale_maid_v2.animation.json'));
assert.deepEqual(source.elements.slice(0, beforeRoot.elements.length),beforeRoot.elements);
for(const previous of beforeRoot.animations) {
  const current=source.animations.find(a=>a.name===previous.name);
  assert.equal(current.length,previous.length);assert.equal(current.loop,previous.loop);
  for(const [id,bone] of Object.entries(previous.animators)) {
    const expected=JSON.parse(JSON.stringify(bone.keyframes));
    if(previous.name.endsWith('.sleep') && bone.name==='root') {
      for(const k of expected.filter(k=>['rotation','position'].includes(k.channel)))
        for(const p of k.data_points)Object.assign(p,{x:0,y:0,z:0});
    }
    assert.deepEqual(current.animators[id].keyframes,expected,'Unexpected animation change: '+previous.name+'/'+bone.name);
  }
  const expectedExport=JSON.parse(JSON.stringify(beforeRootExport.animations[previous.name]));
  if(previous.name.endsWith('.sleep'))for(const channel of ['rotation','position']) {
    for(const [time,v] of Object.entries(expectedExport.bones.root[channel])) {
      if(Array.isArray(v))expectedExport.bones.root[channel][time]=[0,0,0];
      else {if(v.pre)v.pre=[0,0,0];if(v.post)v.post=[0,0,0];}
    }
  }
  assert.deepEqual(anim.animations[previous.name],expectedExport,'Unexpected exported animation change');
}
const beforePot = read(path.join(__dirname, 'backups/before_iron_pot/whale_maid_v2.bbmodel'));
const beforePotGeo = read(path.join(__dirname, 'backups/before_iron_pot/whale_maid_v2.geo.json'))['minecraft:geometry'][0];
const beforePotAnimation = fs.readFileSync(path.join(__dirname, 'backups/before_iron_pot/whale_maid_v2.animation.json'));
assert.deepEqual(source.elements.slice(0, beforePot.elements.length), beforePot.elements, 'Original cubes/UV changed during iron pot edit');
assert.deepEqual(model.bones.filter(b => b.name !== 'iron_pot'), beforePotGeo.bones, 'Original exported bones changed during iron pot edit');
assert.equal(source.elements.length - beforePot.elements.length, 11, 'Unexpected added cube count');
assert(fs.readFileSync(path.join(assets, 'animations/whale_maid_v2.animation.json')).equals(beforePotAnimation), 'Animation export changed during iron pot edit');
for (const previous of beforePot.animations) {
  const current = source.animations.find(a => a.name === previous.name);
  assert.equal(current.length, previous.length);
  assert.equal(current.loop, previous.loop);
  assert.deepEqual(current.animators, previous.animators, 'Animation keyframes changed during iron pot edit: '+previous.name);
}
const baseline = {
  'dev/models/whale_maid/whale_maid.bbmodel':'404C969B740FB73DB0182C0EB29D14CD2B1DB69C0B43C5CC1594D355667DF25C',
  'src/main/resources/assets/funny_cat_stick/geo/whale_maid.geo.json':'16C2F56CCBBC4D29BD16AB6405BCEBF73F63AA05418EA09F6EACC3C6076699E3',
  'src/main/resources/assets/funny_cat_stick/textures/entity/whale_maid.png':'D72D237BFA9726286392F663B3A849119C8D93819B0A2865373C731956E95EDD',
  'src/main/resources/assets/funny_cat_stick/animations/whale_maid.animation.json':'60367A0223CDC75ED980F94DED1AEA963466E0C8877F916BFFA5F3A398CC8CFD'
};
for (const [file, hash] of Object.entries(baseline)) {
  assert.equal(crypto.createHash('sha256').update(fs.readFileSync(path.join(root,file))).digest('hex').toUpperCase(), hash, 'Original changed: '+file);
}
console.log(JSON.stringify({ok:true,cubes,bones:names.size,faces,keys,texture:'1024x1024',old_assets_unchanged:true},null,2));
