/* global module */

module.exports = {
    root: true,
    "extends": "@wrensecurity/eslint-config",
    env: {
        amd: true,
        browser: true,
        es6: true
    },
    overrides: [
        {
            files: [
                "gulpfile.js"
            ],
            env: {
                node: true
            },
            parserOptions: {
                ecmaVersion: 2021
            }
        }
    ]
};
