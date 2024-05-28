-- 获取redis锁中线程标识
local id =redis.call('get',KEYS[1])
-- 判断锁中标识与线程表示是否一致
if(id==ARGV[1]) then
   -- 释放锁
   return redis.call("del",KEYS[1])
end
return 0