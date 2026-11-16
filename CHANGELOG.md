# Changelog

## [2.1.0](https://github.com/yk-higuchi/home-expense-backend/compare/v2.0.0...v2.1.0) (2026-11-16)


### Features

* add db-migration module with Flyway ([fd231b0](https://github.com/yk-higuchi/home-expense-backend/commit/fd231b0969b1c3ef29cfec9bda5bab0173789f2b))
* add smallrye-health dependency for health ([53fee81](https://github.com/yk-higuchi/home-expense-backend/commit/53fee8186de60c508bb6f27e449740c3dbbb96b5))
* **api:** add APIs to reorder expense and income attributes ([b1526da](https://github.com/yk-higuchi/home-expense-backend/commit/b1526dad54e54e2a14d98c15d54f137ce5d7813c))
* **api:** generate JTD schemas from OpenAPI for API consumers ([3f5a5d8](https://github.com/yk-higuchi/home-expense-backend/commit/3f5a5d82340b12f88702dd57c6b210adce815a2d))
* **api:** limit attributes to 100 per order ([1b83664](https://github.com/yk-higuchi/home-expense-backend/commit/1b836640f66aa6db8ea9582aa62de23cf321cd2d))
* **api:** order balance breakdowns by attribute display order ([879f51f](https://github.com/yk-higuchi/home-expense-backend/commit/879f51f852520cf2c9350fad75ea55d9d52308fc))
* **api:** publish a JSON Schema generated from the OpenAPI schemas ([716a282](https://github.com/yk-higuchi/home-expense-backend/commit/716a282a2a4343e8f0df5bef423619ed92b17c04))
* **api:** reorder attributes with JSON Patch on the attribute lists ([aa100f4](https://github.com/yk-higuchi/home-expense-backend/commit/aa100f4c958635bec801f66c2c93a4402e2d0941))
* **api:** require If-Match on DELETE and return version in resources ([3a6ca64](https://github.com/yk-higuchi/home-expense-backend/commit/3a6ca64419028b9ee518145418783258bf4bf9be))
* **api:** return all attributes in display order with an order ETag ([4346cad](https://github.com/yk-higuchi/home-expense-backend/commit/4346cad6fd43d5a90af6f5b83e74d587f440b15d))
* **api:** store the display order of attributes ([352c642](https://github.com/yk-higuchi/home-expense-backend/commit/352c6426df0a23314dadd519363d45fd42d2d2fe))
* **api:** use JCS document digests as ETags ([2f3c522](https://github.com/yk-higuchi/home-expense-backend/commit/2f3c52228768a725646b3e9e2dfd465e29b94c30))
* **app:** add balance-statistics and balance-series endpoints ([6743ccb](https://github.com/yk-higuchi/home-expense-backend/commit/6743ccb26f6341469321ba3020cd4c8a3e5732d3))
* **app:** retry transactions on serialization failure (SQLSTATE 40001) ([dcadb02](https://github.com/yk-higuchi/home-expense-backend/commit/dcadb0211524395b058875c1d4baf4cb3d3504d5))
* fix security vulnerability ([58c2fc6](https://github.com/yk-higuchi/home-expense-backend/commit/58c2fc6dd53791743182bf34d92c8689b4385caa))
* modernize the backend for balance reports, RFC compliance and CockroachDB ([5128df0](https://github.com/yk-higuchi/home-expense-backend/commit/5128df0d1864acbdbc9b715e15772ea080ba9e8b))
* **observability:** propagate W3C trace context in responses and Problem Details ([687ee96](https://github.com/yk-higuchi/home-expense-backend/commit/687ee96d8237a6db6963dac7451d2df42a61ac78))
* remove http-headers doc ([3467a2b](https://github.com/yk-higuchi/home-expense-backend/commit/3467a2b30062a7e41bd8bf70c6acc6290945d687))
* udpate openapi.yaml ([8a48e05](https://github.com/yk-higuchi/home-expense-backend/commit/8a48e054dbef2655980c57fa07929db1481f87bf))
* update library version, change datasource implementation to jdbc ([9bf8161](https://github.com/yk-higuchi/home-expense-backend/commit/9bf81613cac0c0989bffa188270213ff51d99933))


### Bug Fixes

* **api:** align expense/income representations with the API spec ([bed670e](https://github.com/yk-higuchi/home-expense-backend/commit/bed670e1ec5cbe1432566bb6fbcf83a09a78e99f))
* **api:** comply with RFC 9110, RFC 6585, RFC 8288 and RFC 9457 ([b8eaa76](https://github.com/yk-higuchi/home-expense-backend/commit/b8eaa768241408f2c6d0f273bdf33c8a37e2b5e7))
* **api:** conform to the OpenAPI spec for inputs and status codes ([66f80dd](https://github.com/yk-higuchi/home-expense-backend/commit/66f80ddea01239d945d37f5757babddc68b99443))
* **api:** follow RFC 6902 and RFC 5789 more closely for attribute PATCH ([e7be4cf](https://github.com/yk-higuchi/home-expense-backend/commit/e7be4cf7b359999377e86cec75992cf7a1a45e7c))
* **api:** make the expense and income list order deterministic ([6dacfee](https://github.com/yk-higuchi/home-expense-backend/commit/6dacfeec15c96dbc165182ba1261ec7f663295af))
* **api:** reject undefined request properties and document all responses ([ff87fbd](https://github.com/yk-higuchi/home-expense-backend/commit/ff87fbd2bf6d0f5610a868d496ea4dd527ec4b44))
* **app:** make balance-series SQL compatible with CockroachDB ([ccc1dd4](https://github.com/yk-higuchi/home-expense-backend/commit/ccc1dd427e2d4c4db6e418a2d04a599d8a3ce9b3))
* **app:** return 400 instead of 404 on query parameter conversion failure ([1fc32f9](https://github.com/yk-higuchi/home-expense-backend/commit/1fc32f9a87047c65cc83d1fe1d4871598b4c57b1))
* **docs:** add maxLength to balance-series date parameters ([e6ae2a8](https://github.com/yk-higuchi/home-expense-backend/commit/e6ae2a863cd6ead33c9b9fcff6466af326092174))
* **scripts:** keep sort_openapi.sh from adding empty component sections ([c71e1c2](https://github.com/yk-higuchi/home-expense-backend/commit/c71e1c29d2905f052ff678b1d091f075ba6f333e))
* **scripts:** stop running flyway clean before migrate ([6d7cef0](https://github.com/yk-higuchi/home-expense-backend/commit/6d7cef0c9903ab0d52f30f3ff2c24e0c37ce7768))
