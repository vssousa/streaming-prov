package edu.indiana.d2i.prov.streaming;

import org.apache.htrace.fasterxml.jackson.databind.JsonNode;
import org.apache.htrace.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.List;
import java.util.Properties;


public class ProvKafkaProducer {

    private static ProvKafkaProducer provProducer;
    private Producer<String, String> kafkaProducer;
    //    private Producer<String, JsonNode> kafkaProducer;
    private static final String kafkaTopic = "mr-prov";

    private ProvKafkaProducer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//        props.put("key.serializer", "org.apache.kafka.connect.json.JsonConverter");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//        props.put("value.serializer", "org.apache.kafka.connect.json.JsonSerializer");
//        props.put("value.serializer", "org.apache.kafka.connect.json.JsonConverter");

        kafkaProducer = new KafkaProducer<>(props);
//        for (int i = 0; i < 5; i++)
//            provProducer.send(new ProducerRecord<String, String>("connect-test", "line", "message " + Integer.toString(i)));

    }

    public static synchronized ProvKafkaProducer getInstance() {
        if (provProducer == null) {
            provProducer = new ProvKafkaProducer();
        }
        return provProducer;
    }

    public void close() {
        kafkaProducer.close();
    }

    public void createActivity(String id, String function) {
        String notification = "{\"id\":\"" + id +
                "\", \"nodeType\":\"ACTIVITY\", \"type\":\"node\", \"attributes\":{\"function\":\"" +
                function + "\"}}";
        kafkaProducer.send(new ProducerRecord<String, String>(kafkaTopic, "line", notification));
    }

    public void createEntity(String id, String key, String value) {
        String notification = "{\"id\":\"" + id +
                "\", \"nodeType\":\"ENTITY\", \"type\":\"node\", \"attributes\":{\"key\":\"" + key +
                "\", \"value\":\"" + value + "\"}}";
        kafkaProducer.send(new ProducerRecord<String, String>(kafkaTopic, "line", notification));
    }

    public void createEntity(String id) {
        kafkaProducer.send(new ProducerRecord<String, String>(kafkaTopic, "line",
                "{\"id\":\"" + id + "\", \"nodeType\":\"ENTITY\", \"type\":\"node\"}"));
    }

//    public void createEdge(String sourceId, String destId, String edgeType) {
//        String notification = "{\"sourceId\":\"" + sourceId + "\", \"destId\":\"" +
//                destId + "\", \"edgeType\":\"" + edgeType +
//                "\", \"type\":\"edge\"}";
//        kafkaProducer.send(new ProducerRecord<String, String>(kafkaTopic, "line", notification));
//    }

    public void createAndSendEdge(String sourceId, String destId, String edgeType) {
        String notification = "{\"sourceId\":\"" + sourceId + "\", \"destId\":\"" +
                destId + "\", \"edgeType\":\"" + edgeType + "\"}";
//        ObjectMapper objectMapper = new ObjectMapper();
//        JsonNode jsonNode = objectMapper.valueToTree(notification);
        kafkaProducer.send(new ProducerRecord<>(kafkaTopic, "line", notification));
//        kafkaProducer.send(new ProducerRecord<String, JsonNode>(kafkaTopic, jsonNode));
    }

    public String createEdge(String sourceId, String destId, String edgeType) {
        return "{\"sourceId\":\"" + sourceId + "\", \"destId\":\"" +
                destId + "\", \"edgeType\":\"" + edgeType + "\"}";
    }

    public void createAndSendJSONArray(List<String> notifications, String edgeType) {
        if (notifications.size() == 1)
            kafkaProducer.send(new ProducerRecord<>(kafkaTopic, "line", notifications.get(0)));
        else if (notifications.size() > 1) {
            StringBuilder array = new StringBuilder("{\"group\":[");
            for (int i = 0; i < notifications.size(); i++) {
                array.append(notifications.get(i));
                if (i < notifications.size() - 1)
                    array.append(", ");
            }
            array.append("], \"edgeType\":\"").append(edgeType).append("\"}");
            kafkaProducer.send(new ProducerRecord<>(kafkaTopic, "line", array.toString()));
        }
    }

}
