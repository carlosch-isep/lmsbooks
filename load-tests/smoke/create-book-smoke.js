import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  vus: 1,
  duration: '10s',
};

export default function () {
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
    headers: { 'Content-Type': 'application/json' },
  };

  let res = http.post('http://localhost:8087/api/books', payload, params);
  check(res, { 'created': (r) => r.status === 201 });
  sleep(1);
}

