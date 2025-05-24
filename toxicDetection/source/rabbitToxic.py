import pika
import json
import tensorflow as tf
from tensorflow.keras.preprocessing.sequence import pad_sequences
from preprocess import clean_text, MAX_SEQUENCE_LENGTH
import pickle

# Load model và tokenizer
model = tf.keras.models.load_model('/models/lstm_toxic_model_binary.h5')
with open('/models/tokenizer.pkl', 'rb') as f:
    tokenizer = pickle.load(f)

def predict_toxicity(text, threshold=0.5):
    text_clean = clean_text(text)
    seq = tokenizer.texts_to_sequences([text_clean])
    pad_seq = pad_sequences(seq, maxlen=MAX_SEQUENCE_LENGTH)
    pred = model.predict(pad_seq)[0][0]
    return pred > threshold  

def main():
    credentials = pika.PlainCredentials('admin', 'admin')  # dùng đúng username & password
    parameters = pika.ConnectionParameters(host='localhost', 
                                           port=5672,
                                           credentials=credentials)

    connection = pika.BlockingConnection(parameters)
    channel = connection.channel()

    input_queue = 'send-CheckToxic'
    channel.queue_declare(queue=input_queue, durable=True)
    channel.queue_bind(queue=input_queue,
                   exchange='send-CheckToxic',
                   routing_key='send-CheckToxic')

    queue_toxic_true = 'send-CheckToxicTrue'
    queue_toxic_false = 'send-CheckToxicFalse'
    channel.queue_declare(queue=queue_toxic_true, durable=True)
    channel.queue_declare(queue=queue_toxic_false, durable=True)

    def callback(ch, method, properties, body):
        try:
            event = json.loads(body)
            comment_id = event.get('commentId')
            comment_desc = event.get('commentDescription')
            print(f"[Received] id={comment_id} desc={comment_desc}")

            toxic = predict_toxicity(comment_desc)

            result_event = {
                "commentId": comment_id,
                "isToxic": bool(toxic)
            }
            result_msg = json.dumps(result_event)

            if toxic:
                channel.basic_publish(
                    exchange=queue_toxic_true,
                    routing_key='',
                    body=result_msg,
                    properties=pika.BasicProperties(delivery_mode=2)  # persistent
                )
                print(f"[Sent] To {queue_toxic_true}: {result_msg}")
            else:
                channel.basic_publish(
                    exchange=queue_toxic_false,
                    routing_key='',
                    body=result_msg,
                    properties=pika.BasicProperties(delivery_mode=2)
                )
                print(f"[Sent] To {queue_toxic_false}: {result_msg}")

            ch.basic_ack(delivery_tag=method.delivery_tag)
        except Exception as e:
            print(f"[Error] {e}")
            ch.basic_ack(delivery_tag=method.delivery_tag)

    channel.basic_qos(prefetch_count=1)
    channel.basic_consume(queue=input_queue, on_message_callback=callback)
    print("[*] Waiting for messages...")
    channel.start_consuming()

if __name__ == "__main__":
    main()
