from flask import Flask, request, jsonify
from main import EnhancedAIChatbot  # Import class chatbot từ main.py

app = Flask(__name__)

# Khởi tạo chatbot 1 lần khi chạy server
chatbot = EnhancedAIChatbot()

@app.route('/chat', methods=['POST'])
def chat():
    data = request.get_json()
    if not data or 'query' not in data:
        return jsonify({'error': "Missing 'query' in JSON body"}), 400

    user_query = data['query']

    try:
        response = chatbot.chat(user_query)
        return jsonify({'answer': response})
    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/stats', methods=['GET'])
def stats():
    stats_data = chatbot.get_vector_db_stats()
    return jsonify(stats_data)

@app.route('/shutdown', methods=['POST'])
def shutdown():
    chatbot.close()
    func = request.environ.get('werkzeug.server.shutdown')
    if func:
        func()
    return "Server shutting down..."

if __name__ == '__main__':
    print("🚀 Starting Flask API server for Enhanced AI Chatbot...")
    app.run(host='0.0.0.0', port=5001, debug=True)
