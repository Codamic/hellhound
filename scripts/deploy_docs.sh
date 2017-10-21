#! /bin/bash

echo "Building API docs..."
lein codox
echo "Building the Guides..."
pushd docs/guides
bundle install
rake compile
popd

mkdir -p ./build/guides
cp -rv docs/guides/_build ./build/guides
cp -rv docs/guides/CNAME ./build/
cp -rv docs/api/ ./build/
echo "Deploying"
git branch -D gh-pages
git checkout --orphan gh-pages
git rm -rf ./
rm -rf node_modules
rm pom.xml
rm -rf docs/_build/
rm -rf target
rm -rf ./scripts
cp -rv build/* ./
rm -rf build/
git add .
now=`date +%s`
git commit -a -m "Release at $now"
git push origin gh-pages -f
git checkout master
echo "Done"
