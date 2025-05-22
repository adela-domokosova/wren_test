/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.1.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.1.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2024 Wren Security.
 */

const {
    useEslint,
    useLocalResources,
    useLessStyles,
    useBuildRequire
} = require("@wrensecurity/commons-ui-build");
const gulp = require("gulp");
const { join } = require("path");

const TARGET_PATH = "target/www";

const DEPLOY_DIR = `../forgerock-selfservice-example/target/webapp`;

gulp.task("eslint", useEslint());

gulp.task("build:assets", useLocalResources({ "src/main/resources/**": "" }, { dest: TARGET_PATH }));

gulp.task("build:scripts", useLocalResources({ "src/main/js/**/*.js": "" }, { dest: TARGET_PATH }));

gulp.task("build:compose", useLocalResources({ "target/ui-compose/**": "" }, { dest: TARGET_PATH }));

gulp.task("build:styles", useLessStyles({
    "target/www/css/structure.less": "css/structure.css",
    "target/www/css/theme.less": "css/theme.css"
}, { base: join(TARGET_PATH, "css"), dest: TARGET_PATH }));

gulp.task("build:bundle", useBuildRequire({
    base: TARGET_PATH,
    src: "src/main/js/main.js",
    dest: join(TARGET_PATH, "main.js"),
    exclude: [
        // Excluded from optimization so that the UI can be customized without having to repackage it.
        "config/AppConfiguration"
    ]
}));

gulp.task("build", gulp.series(
    gulp.parallel(
        "build:assets",
        "build:scripts",
        "build:compose"
    ),
    gulp.parallel(
        "build:styles",
        "build:bundle"
    )
));

gulp.task("deploy", () => gulp.src(`${TARGET_PATH}/**/*`).pipe(gulp.dest(DEPLOY_DIR)));

gulp.task("watch", () => {
    gulp.watch(
        ["src/main/resources/**", "src/main/js/**/*.js", "target/ui-compose/**"],
        gulp.series("build", "deploy")
    );
});

gulp.task("dev", gulp.series("build", "deploy", "watch"));
gulp.task("prod", gulp.series("eslint", "build"));

gulp.task("default", gulp.series("dev"));
