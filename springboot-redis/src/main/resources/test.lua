---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by asus.
--- DateTime: 2024/5/8 11:07
---
local voucherId = KEYS[1]
local userId = ARGV[1]
local stockKey = 'redis:kill:voucher:' .. voucherId
local orderKey='redis:order:'..voucherId
if (tonumber(redis.call('hget', stockKey, 'stock')) < 0) then
    return 0
end
if(tonumber(redis.call('sismember',orderKey,userId))) then
    return 1
end
--if(tonumber())
redis.call('sadd', 'redis:order:2', 6)
return redis.call('hincrby', stockKey, 'stock', -1)