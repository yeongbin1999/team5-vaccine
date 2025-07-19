-- KEYS[1] = redisKey (예: refresh:{userId})
-- ARGV[1] = oldToken
-- ARGV[2] = newToken
-- ARGV[3] = expiration (ms)

local storedToken = redis.call('get', KEYS[1])

if storedToken == ARGV[1] then
  redis.call('set', KEYS[1], ARGV[2], 'PX', ARGV[3])
  return 1
else
  return 0
end
