/* test tables */

-- http://sqlfiddle.com/#!9/f7f1fa/1

create table t1 (
  id int,
  name varchar(30)
);


insert into t1 values
(1, 'hey'),
(2, 'yoo');


select * from t1;

select id + 200 - 30, name, MOD(29,9) from t1;

SELECT MOD(29,9);