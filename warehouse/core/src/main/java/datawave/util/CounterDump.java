package datawave.util;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import datawave.util.CounterDump.CounterSource;

import org.apache.accumulo.core.client.AccumuloException;
import org.apache.accumulo.core.client.AccumuloSecurityException;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.CounterGroup;
import org.apache.hadoop.mapreduce.Counters;

import com.google.common.collect.Maps;

public class CounterDump {
    
    private CounterSource source;
    
    public CounterDump(CounterSource source) {
        this.source = source;
    }
    
    public String toString() {
        StringBuilder builder = new StringBuilder();
        
        while (source.hasNext()) {
            Entry<String,Counters> nextCntr = source.next();
            builder.append("\n").append(nextCntr.getKey()).append("\n----------------------\n");
            Counters counters = nextCntr.getValue();
            
            for (String groupName : counters.getGroupNames()) {
                
                CounterGroup group = counters.getGroup(groupName);
                Iterator<Counter> cntrItr = group.iterator();
                while (cntrItr.hasNext()) {
                    Counter counter = cntrItr.next();
                    builder.append(groupName).append("\t").append(counter.getDisplayName()).append("=").append(counter.getValue()).append("\n");
                }
                
            }
        }
        
        return builder.toString();
    }
    
    public static abstract class CounterSource implements Iterator<Entry<String,Counters>> {
        public abstract String getNextIdentifier();
        
        public abstract byte[] getNextCounterData();
        
        @Override
        public Entry<String,Counters> next() {
            Counters cntrs = new Counters();
            ByteArrayInputStream input = new ByteArrayInputStream(getNextCounterData());
            DataInputStream dataInput = new DataInputStream(input);
            try {
                cntrs.readFields(dataInput);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            
            return Maps.immutableEntry(getNextIdentifier(), cntrs);
        }
        
    }
    
}
