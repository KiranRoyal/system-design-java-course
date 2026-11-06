create table products (id bigint primary key, name varchar(100) not null, stock integer not null check (stock >= 0), version bigint not null);
create table reservations (request_key varchar(80) primary key, product_id bigint not null references products(id), quantity integer not null check (quantity > 0));
insert into products(id,name,stock,version) values (1,'Java Course Workbook',10,0);
