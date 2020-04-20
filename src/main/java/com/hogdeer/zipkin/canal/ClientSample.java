package com.hogdeer.zipkin.canal;

import java.net.InetSocketAddress;
import java.util.List;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.common.utils.AddressUtils;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.alibaba.otter.canal.protocol.CanalEntry.Column;
import com.alibaba.otter.canal.protocol.CanalEntry.Entry;
import com.alibaba.otter.canal.protocol.CanalEntry.EntryType;
import com.alibaba.otter.canal.protocol.CanalEntry.EventType;
import com.alibaba.otter.canal.protocol.CanalEntry.RowChange;
import com.alibaba.otter.canal.protocol.CanalEntry.RowData;
import com.alibaba.otter.canal.client.*;
import com.google.protobuf.InvalidProtocolBufferException;

import javax.validation.constraints.NotNull;

public class ClientSample {

    public static void main(String args[]) {
        // 创建链接
       // CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress(AddressUtils.getHostIp(),11111), "example", "", "");

        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress("47.103.45.158",11111), "example", "", "");
        int batchSize = 1000;
        int emptyCount = 0;
        try {
            connector.connect();
            connector.subscribe(".*\\..*");
            connector.rollback();
            int totalEmtryCount = 1200;
            while (emptyCount < totalEmtryCount) {
                Message message = connector.getWithoutAck(batchSize); // 获取指定数量的数据
                long batchId = message.getId();
                int size = message.getEntries().size();
                if (batchId == -1 || size == 0) {
                    emptyCount++;
                    System.out.println("empty count : " + emptyCount);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    emptyCount = 0;
                     System.out.printf("message[batchId=%s,size=%s] \n", batchId, size);
                    printEntry(message.getEntries());
                }
                connector.ack(batchId); // 提交确认
                // connector.rollback(batchId); // 处理失败, 回滚数据
            }
            System.out.println("empty too many times, exit");
        } finally {
            connector.disconnect();
        }
    }

    private static void printEntry(@NotNull List<Entry> entrys) {
        for (Entry entry : entrys) {
//            System.out.println("----Gtid------------------->"+  entry.getHeader().getGtid());
//            System.out.println("----Header------------------->"+  entry.getHeader().toString());
//            System.out.println("entryType   -------------> " + entry.getEntryType());
            if (entry.getEntryType() == EntryType.TRANSACTIONBEGIN
                    || entry.getEntryType() == EntryType.TRANSACTIONEND) {

                CanalEntry.TransactionBegin begin = null;

                try {
                    begin = CanalEntry.TransactionBegin.parseFrom(entry.getStoreValue());
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException("parse event has an error , data:" + entry.toString(), e);
                }
                // 打印事务头信息，执行的线程id，事务耗时
//                logger.info(transaction_format,
//                        new Object[]{entry.getHeader().getLogfileName(),
//                                String.valueOf(entry.getHeader().getLogfileOffset()),
//                                String.valueOf(entry.getHeader().getExecuteTime()),
//                                simpleDateFormat.format(date),
//                                entry.getHeader().getGtid(),
//                                String.valueOf(delayTime)});
              //  logger.info(" BEGIN ----> Thread id: {}", begin.getThreadId());
//                System.out.println((" BEGIN ----> Thread id: "+ begin.getThreadId()));

                System.out.println(" BEGIN ----> transaction id: "+ begin.getTransactionId());






//                CanalEntry.TransactionEnd end = null;
//                try {
//                    end = CanalEntry.TransactionEnd.parseFrom(entry.getStoreValue());
//                } catch (InvalidProtocolBufferException e) {
//                    throw new RuntimeException("parse event has an error , data:" + entry.toString(), e);
//                }
//                // 打印事务提交信息，事务id
//
//                System.out.println(" END ----> transaction id:"+ end.getTransactionId());


                continue;
            }

            RowChange rowChage = null;


            try {
                rowChage = RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                throw new RuntimeException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(), e);
            }

            EventType eventType = rowChage.getEventType();
//            CanalEntry.Header header = entry.getHeader();
//            System.out.println("header===========> " + header.toString());
            System.out.println(String.format("================> binlog[%s:%s] , name[%s,%s] , eventType : %s", entry.getHeader().getLogfileName(), entry.getHeader().getLogfileOffset(),

                    entry.getHeader().getSchemaName(), entry.getHeader().getTableName(),

                    eventType));


          //  if (eventType ==EventType.QUERY){
                System.out.println("sql -------> " +rowChage.getSql());
           // }

            for (RowData rowData : rowChage.getRowDatasList()) {
                if (eventType == EventType.DELETE) {
                    printColumn(rowData.getBeforeColumnsList());
                } else if (eventType == EventType.INSERT) {
                    printColumn(rowData.getAfterColumnsList());
                } else {
                    System.out.println("-------> before");
                    printColumn(rowData.getBeforeColumnsList());
                    System.out.println("-------> after");
                    printColumn(rowData.getAfterColumnsList());
                }
            }
        }
    }

    private static void printColumn(@NotNull List<Column> columns) {
        for (Column column : columns) {
            System.out.println(column.getName() + " : " + column.getValue() + " update=" + column.getUpdated() +" isKey =" +column.getIsKey() + " sqlType="+column.getSqlType());
        }
    }
}
