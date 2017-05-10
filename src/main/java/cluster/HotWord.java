package cluster;

import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import org.apache.storm.redis.bolt.AbstractRedisBolt;
import org.apache.storm.redis.common.config.JedisPoolConfig;
import redis.clients.jedis.JedisCommands;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static utils.Utils.json2Map;

/**
 * Created by jinha on 2017/5/11.
 */
public class HotWord  extends AbstractRedisBolt {
    public HotWord(JedisPoolConfig config) {
        super(config);
    }

    public void execute(Tuple tuple) {
        JedisCommands jedisCommands = null;
        try {
            jedisCommands = getInstance();
            long limit = jedisCommands.llen("cluster");
            Set<Integer> classSet = new HashSet<Integer>();
            Map<String,Double> tf =  json2Map(tuple.getStringByField("tf"));
            for(String s :tf.keySet()){
                String csSet = jedisCommands.hget("hotWord" , s);
                if(csSet==null||csSet.equals(""))
                    continue;
                for(String s1:csSet.split(","))
                    classSet.add(Integer.parseInt(s1));
            }
            this.collector.emit(new Values(tuple.getIntegerByField("Id"),tuple.getStringByField("tf"),classSet.toString(),limit));

        } finally {
            if (jedisCommands != null) {
                returnInstance(jedisCommands);
            }
            this.collector.ack(tuple);
        }
    }

    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("Id","tf","classList","limit"));
    }
}