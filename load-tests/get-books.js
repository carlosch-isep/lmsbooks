import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    vus: 1,
    duration: '1s',
};

export default function () {

    const TOKEN = 'MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAiuZ4N2VZ8bo95gLg/tyV' +
        'p6hEaR7NnXqGsPdg7iWVEnHLMEMEpxKKSRqies2xgqJYK+vqdXF5qmIc9arMsKQQ' +
        'wHW8U3uMUtfAE5XMjgX0eUv4MrZOexJViUxgHpWo214J3tq/+hXuuoFnz514q18d' +
        '413FW3l674+S7ISVjvrYQeI10IHfhXPG8YjXTtZl96ZeNN7Kfdn+twn/RyuBNaOE' +
        'shvRhklk46BPFJYUQvh6W/KpA5c9csNj0QwJDqquwugM38GfSUaOYki9nBz+cYJW' +
        'TwsA7ZjF53my606Ml1iUspkpypVSWOd9MAUjGOysLOQL//kpkIAhpKAHkKA0qTwa' +
        'HtkrCyZLghTxHq6i89oMZFOdl2AAxz7AbB2xGPexitYc1gchGTnsKVvkMqe/IdNh' +
        'I8T6YJAwVZ8kp2CmyMblg7+4XQORedSnY/Hdi19ljaBkkiUDOlsMcsY+2Da07O5G' +
        'ojpVJLsjnlE4KjHNi9vdSOENS2Qlnuty2X/NziWDAzrnhsINMaOEuwwWco+8HYrv' +
        'lqichDEK2colbDpxWNBqBpTHF4p+jtBVzIomXFTm7r5PRaPrVcP8yMF5TWs9jwUq' +
        'EqjWnErcnmNb9F3AeocFi8DTd6x6YxXA9G8YMdx1JEKiggIPu587viYxiFEgSmC+' +
        'DSHH49SuJzUozyddXZG0A5kCAwEAAQ==';

    let params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization' : 'Bearer ' + TOKEN
        },
    };

    const url = 'http://localhost:8088/api/query/books?title=The%20Art%20of%20Clean%20Code';

    let res = http.get(url, params);

    check(res, {
        'status was 200': (r) => r.status === 200,
        'body size > 0': (r) => r.body.length > 0
    });

    sleep(1);
}