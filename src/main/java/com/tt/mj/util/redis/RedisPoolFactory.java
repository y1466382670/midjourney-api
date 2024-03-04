package com.tt.mj.util.redis;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
@EnableRedisRepositories
public class RedisPoolFactory {

    @Autowired
    RedisConfig  redisConfig;

    /**
     * 将redis连接池注入spring容器
     * @return
     */
    @Bean
    public JedisPool JedisPoolFactory(){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(500);
        config.setMaxTotal(1000);
        config.setMaxWaitMillis(500 * 1000);
        String psw = redisConfig.getPassword();
        if(StringUtils.isEmpty(psw)){
        	psw = null;
        }
        
        JedisPool jp = new JedisPool(config, redisConfig.getHost(), redisConfig.getPort(),
                redisConfig.getTimeout()*1000, redisConfig.getPassword(), redisConfig.getDatabase());
        return jp;
    }

}
