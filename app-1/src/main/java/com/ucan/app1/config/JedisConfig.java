package com.ucan.app1.config;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.JedisPooled;

/**
 * @Description: Jedis客户端配置
 * @author liming.cen
 * @date 2024-08-03 18:17:55
 * 
 */
@Configuration
public class JedisConfig {
    private static Logger log = LoggerFactory.getLogger(JedisConfig.class);
    @Value("${ucan.redis.host}")
    private String host;
    @Value("${ucan.redis.port}")
    private int port;

    @Bean
    @ConditionalOnMissingBean
    public JedisPooled jedis() {

        // jedis连接池配置
        ConnectionPoolConfig poolConfig = new ConnectionPoolConfig();
        // jedis最大活跃连接数
        poolConfig.setMaxTotal(30);

        // jedis最大闲置连接数
        poolConfig.setMaxIdle(8);
        // jedis最小闲置连接数
        poolConfig.setMinIdle(1);

        // 允许等待连接可用
        poolConfig.setBlockWhenExhausted(true);
        // 连接最大等待时长，单位：秒
        poolConfig.setMaxWait(Duration.ofSeconds(1));

        // 允许闲置连接定期发送ping 命令到redis服务端
        poolConfig.setTestWhileIdle(true);
        // 闲置连接发送ping 检查的周期，单位：秒
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofSeconds(1));

        // 自动管理jedis连接的获取与释放
        JedisPooled jedis = new JedisPooled(poolConfig, host, port);
        log.info("Redis 已连接，服务器地址：" + host + "，端口号：" + port);
        return jedis;
    }
}
