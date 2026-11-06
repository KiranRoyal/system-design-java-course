from harness import *
checks=0
def check(condition,label):
 global checks
 assert condition,label
 checks+=1;print('PASS',label,flush=True)
inventory_port=freeport();orders_port=freeport()
with service('inventory',inventory_port) as inv,service('orders',orders_port,[f'--inventory.base-url={inv}']) as orders:
 code,products=request(inv,'/products');check(code==200 and products[0]['stock']==10,'seed product and stock')
 body={'productId':1,'quantity':2};code,first=request(orders,'/orders',body,'same');check(code==200 and first['status']=='CONFIRMED','real two-service checkout')
 code,replay=request(orders,'/orders',body,'same');check(code==200 and replay['id']==first['id'],'idempotent order replay')
 check(request(inv,'/products')[1][0]['stock']==8,'one inventory effect')
 check(request(orders,'/orders',{'productId':1,'quantity':3},'same')[0]==409,'different intent conflicts')
 check(request(orders,'/orders',{'productId':1,'quantity':0},'bad')[0]==400,'bean validation rejects zero quantity')
 check(request(orders,'/orders/'+first['id'])[1]['status']=='CONFIRMED','read order DTO')
 check(request(orders,'/orders/missing')[0]==404,'missing order')
 check(request(inv,'/products?size=101')[0]==400,'bounded pagination')
 check(request(inv,'/reservations',{'productId':999,'quantity':1},'unknown')[0]==404,'missing inventory product')
 check(request(inv,'/reservations',{'productId':1,'quantity':99},'large')[0]==409,'insufficient stock')
 check(request(inv,'/products')[1][0]['stock']==8,'failed calls leave stock unchanged')
print('HTTP checks passed:',checks)
