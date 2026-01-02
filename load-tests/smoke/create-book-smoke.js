import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  vus: 1,
  duration: '10s',
};

export default function () {
  let payload = JSON.stringify({
    title: `Smoke Book ${__VU}-${__ITER}`,
    author: "Smoke Test Author",
    isbn: `978-3-16-148410-${__VU}`,
    description: "Smoke test book"
  });

  let params = {
    headers: { 'Content-Type': 'application/json' },
  };

  let res = http.post('http://localhost:8087/api/books', payload, params);
  check(res, { 'created': (r) => r.status === 201 });
  sleep(1);
}

