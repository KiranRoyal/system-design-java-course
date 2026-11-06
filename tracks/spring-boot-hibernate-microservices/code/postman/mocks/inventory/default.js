/** Local learning simulation. Contract: contracts/inventory.openapi.json.
 * __reset and __scenario are test-only controls, never production endpoints.
 */
const http = require('http');
let serial = Promise.resolve();
const server = http.createServer((req, res) => {
  serial = serial.then(async () => {
    const url = new URL(req.url, 'http://localhost');
    const send = (status, value) => { res.writeHead(status, {'Content-Type':'application/json'}); res.end(JSON.stringify(value)); };
    const error = (status, detail) => send(status, {status, detail});
    let state = await pm.state.get('inventory:state');
    if (!state) state = {stock:10, version:0, reservations:{}, mode:'success'};
    const body = req.body || {};
    if (req.method === 'POST' && url.pathname === '/__reset') {
      await pm.state.set('inventory:state', {stock:10, version:0, reservations:{}, mode:'success'});
      return send(200, {reset:true});
    }
    if (req.method === 'PUT' && url.pathname === '/__scenario') {
      if (!['success','outage','delay'].includes(body.mode)) return error(400,'invalid scenario');
      state.mode=body.mode;await pm.state.set('inventory:state',state);return send(200,{mode:state.mode});
    }
    if (req.method === 'GET' && url.pathname === '/products') {
      const page=Number(url.searchParams.get('page') || 0), size=Number(url.searchParams.get('size') || 20);
      if (!Number.isInteger(page)||page<0||!Number.isInteger(size)||size<1||size>100) return error(400,'invalid page or size');
      return send(200,page===0?[{id:1,name:'Java Course Workbook',stock:state.stock,version:state.version}]:[]);
    }
    if (req.method !== 'POST' || url.pathname !== '/reservations') return error(404,'unknown endpoint');
    const key=req.headers['idempotency-key'];
    if (!key || !/^[A-Za-z0-9_-]{1,80}$/.test(key) || !Number.isInteger(body.productId) || body.productId<1 || !Number.isInteger(body.quantity)||body.quantity<1||body.quantity>1000) return error(400,'invalid reservation');
    if (state.mode==='outage') return error(503,'simulated inventory outage');
    if (body.productId!==1) return error(404,'product not found');
    const previous=state.reservations[key];
    if (previous && (previous.productId!==body.productId || previous.quantity!==body.quantity)) return error(409,'key reused with different request');
    if (!previous) {
      if (body.quantity>state.stock) return error(409,'insufficient stock');
      state.stock-=body.quantity;state.version++;
      state.reservations[key]={productId:body.productId,quantity:body.quantity};
      await pm.state.set('inventory:state',state);
    }
    // Commit the simulated effect BEFORE delaying the reply: demonstrates unknown outcomes.
    if (state.mode==='delay') await new Promise(resolve=>setTimeout(resolve,3000));
    send(200,{key,productId:body.productId,quantity:body.quantity,remaining:state.stock});
  }).catch(() => { if (!res.headersSent) res.writeHead(500,{'Content-Type':'application/json'});res.end('{"status":500,"detail":"mock failure"}'); });
});
server.listen(process.env.PORT || 18083);
