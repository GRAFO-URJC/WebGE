insert into webge.user(username,password,email,enabled)
values ('admin','$2a$11$hwnvHO4u./7PBsClAXe1fuPIat1sqitn7EYvti9ajWpONIqx7pYB2','emailGenerado@emailGenerado.com',true);

insert into webge.role(role)
values ('ROLE_ADMIN');


insert into webge.users_roles(user_id,username,role_id,role_name,email)
select user_id,username, -1 as role_id, 'ROLE_ADMIN' as role_name,email from webge.user ;


update webge.users_roles
set role_id = role.role_id
from
webge.role
