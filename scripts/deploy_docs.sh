#! /bin/bash

echo "Cleaning up..."
rm -rf ./build
echo "Building API docs..."
lein codox
echo "Building the Guides..."
pushd docs/guides
bundle install
bundle exec rake compile
ls
popd

mkdir -p ./build/
mv -v docs/guides/_build ./build/
mv -v ./build/_build ./build/guides
cp -rv docs/guides/CNAME ./build/
mv -v docs/api/ ./build/
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
