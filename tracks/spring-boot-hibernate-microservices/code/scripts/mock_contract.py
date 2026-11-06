from harness import request
base='http://127.0.0.1:18083'
request(base,'/__reset',{},method='POST')
assert request(base,'/products')[1][0]['stock']==10
assert request(base,'/reservations',{'productId':1,'quantity':2},'m1')[0]==200
assert request(base,'/reservations',{'productId':1,'quantity':2},'m1')[0]==200
assert request(base,'/products')[1][0]['stock']==8
assert request(base,'/reservations',{'productId':1,'quantity':3},'m1')[0]==409
assert request(base,'/reservations',{'productId':999,'quantity':1},'missing')[0]==404
assert request(base,'/reservations',{'productId':1,'quantity':0},'invalid')[0]==400
assert request(base,'/reservations',{'productId':1,'quantity':99},'large')[0]==409
assert request(base,'/products?size=101')[0]==400
request(base,'/__scenario',{'mode':'outage'},method='PUT')
assert request(base,'/reservations',{'productId':1,'quantity':1},'outage')[0]==503
request(base,'/__reset',{},method='POST')
print('Mock contract checks passed: 10')
