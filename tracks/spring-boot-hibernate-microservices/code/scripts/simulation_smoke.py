from harness import *
base='http://127.0.0.1:18083';request(base,'/__reset',{},method='POST');port=freeport();checks=0
with service('orders',port,[f'--inventory.base-url={base}']) as orders:
 request(base,'/__scenario',{'mode':'outage'},method='PUT')
 code,pending=request(orders,'/orders',{'productId':1,'quantity':2},'recover');assert code==202 and pending['status']=='PENDING';checks+=1
 request(base,'/__scenario',{'mode':'success'},method='PUT')
 code,done=request(orders,'/orders',{'productId':1,'quantity':2},'recover');assert code==200 and done['id']==pending['id'] and done['status']=='CONFIRMED';checks+=1
 assert request(base,'/products')[1][0]['stock']==8;checks+=1
 request(base,'/__scenario',{'mode':'delay'},method='PUT')
 start=time.monotonic();code,slow=request(orders,'/orders',{'productId':1,'quantity':1},'slow');elapsed=time.monotonic()-start
 assert code==202 and slow['status']=='PENDING' and elapsed<5;checks+=1
 time.sleep(1.5) # Let the deliberately delayed remote effect complete.
 request(base,'/__scenario',{'mode':'success'},method='PUT')
 code,recovered=request(orders,'/orders',{'productId':1,'quantity':1},'slow');assert code==200 and recovered['id']==slow['id'];checks+=1
 assert request(base,'/products')[1][0]['stock']==7;checks+=1
print('Real order service against Postman mock checks passed:',checks)
