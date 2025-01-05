// import necessary modules
import {check} from 'k6';
import http from 'k6/http';

// define configuration
export const options = {
    // define thresholds
    // thresholds: {
    //     http_req_failed: ['rate<0.01'], // http errors should be less than 1%
    //     http_req_duration: ['p(99)<1000'], // 99% of requests should be below 1s
    // },
    // define scenarios
    scenarios: {
        // arbitrary name of scenario
        average_load: {
            executor: 'ramping-vus',
            stages: [
                // ramp up to average load of 20 virtual users
                {duration: '30s', target: 10},
                // maintain load
                {duration: '50s', target: 20},
                {duration: '5m', target: 40},
                {duration: '5m', target: 100},
                {duration: '5m', target: 200},
                {duration: '10m', target: 150},
                {duration: '2m', target: 100},
                {duration: '2m', target: 120},
                {duration: '30s', target: 80},
                {duration: '30s', target: 40},
                {duration: '30s', target: 20},
            ],
        },
    },
};

function getRandomInteger(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

export default function () {
    // define URL and request body
    const host = '13.212.191.133:8080'
    const url = http.url`http://${host}/movie/${getRandomInteger(1, 2_000_000)}`

    // send a post request and save response as a variable
    const res = http.get(url,
        {tags: {name: `movie`}}
    );

    // check that response is 200
    check(res, {
        'response code was 200': (res) => res.status == 200,
    });
}