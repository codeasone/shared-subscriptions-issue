package io.github.codeasone;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Arrays;

class MessageListener implements IMqttMessageListener, MqttCallback {
    public void connectionLost(Throwable cause) {}
    public void messageArrived(String topic, MqttMessage message) throws Exception {}
    public void deliveryComplete(IMqttDeliveryToken token) {}
}

public class SharedSubscriptionsIssue {
    public static void main(String[] args) {

        String sharedTopic     = "$share:group:my/topic";
        String publishingTopic = "my/topic";
        int    qos             = 0;
        String broker          = "tcp://localhost:1883";
        String sid1            = "subcriber1";
        String sid2            = "subcriber2";
        String pid             = "publisher";

        MemoryPersistence persistence = new MemoryPersistence();

        try {
            final MqttClient sub1      = new MqttClient(broker, sid1, persistence);
            final MqttClient sub2      = new MqttClient(broker, sid2, persistence);
            final MqttClient publisher = new MqttClient(broker, pid);

            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: " + broker);

            sub1.connect(connOpts);
            sub2.connect(connOpts);

            ///////////////////////////////////////////////////////////////////
            // Uncomment GOOD or BAD blocks according to requirements
            //
            // GOOD: Calls back to listener code with these set!
            // sub1.setCallback(simpleListener(sub1.getClientId()));
            // sub2.setCallback(simpleListener(sub2.getClientId()));
            // sub1.subscribe(sharedTopic, qos);
            // sub2.subscribe(sharedTopic, qos);

            // BAD: Doesn't callback into listener code via this route
            sub1.subscribe(sharedTopic, qos, simpleListener(sub1.getClientId()));
            sub2.subscribe(sharedTopic, qos, simpleListener(sub2.getClientId()));

            ///////////////////////////////////////////////////////////////////

            publisher.connect(connOpts);
            for (int i = 1; i < 9; i++) {
                byte e = (byte) i;
                publisher.publish(publishingTopic, new MqttMessage(new byte[]{e}));
                System.out.println("Published: " + i);
                Thread.sleep(500);
            }

            System.out.println("Disconnected");

            sub1.disconnect();
            sub2.disconnect();

            System.out.println("All done.");

        } catch (MqttException | InterruptedException me) {
            System.out.println("msg "   + me.getMessage());
            System.out.println("loc "   + me.getLocalizedMessage());
            System.out.println("cause " + me.getCause());
            System.out.println("excep " + me);
            me.printStackTrace();
        }

        System.exit(0);
    }

    static MessageListener simpleListener(final String id) {
        return new MessageListener() {
            @Override
            public void messageArrived(String topic, MqttMessage message)
                    throws Exception {
                System.out.println("Received on [" + topic + "]: " +
                                   Arrays.toString(message.getPayload()) +
                                   " for client: [" + id + "]");
                Thread.sleep(500);
            }
        };
    }
}
