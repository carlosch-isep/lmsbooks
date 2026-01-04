import http from 'k6/http';
import { check, sleep } from 'k6';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";

export const options = {
  stages: [
    { duration: '1s', target: 10 }
  ],
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<7000'],
  },
};

export default function () {

  const url = __ENV.BASE_URL || 'http://lms-isep.ovh';

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

  let payload = JSON.stringify({
    isbn: "9783161484100",
    title: "The Art of Clean Code",
    genre: "Software Development",
    description: "A comprehensive guide to writing maintainable and efficient software.",
    photoURI: "uploads/books/covers/clean-code.jpg",
    authors: [
      {
        "name": "John Doe",
        "bio": "A veteran software architect with 20 years of experience."
      },
      {
        "name": "Jane Smith",
        "bio": "Open source contributor and technical writer."
      }
    ]
  });

  let params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization' : 'Bearer ' + TOKEN
    },
  };

  let res = http.post(`${url}/api/query/books`, payload, params);

  let checkRes = check(res, { 'created': (r) => r.status === 200 });

  if (!checkRes || res.status !== 200) {
    console.error(`ERRO: Status ${res.status} | URL: ${res.url} | Body: ${res.body}`);
  }

  sleep(2);
}

// Create Report
export function handleSummary(data) {
  return {
    "summary.html": htmlReport(data),
  };
}