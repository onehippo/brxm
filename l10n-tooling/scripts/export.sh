#!/bin/bash

#  Copyright 2018 Hippo B.V. (http://www.onehippo.com)
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

locales="nl de fr zh es"
release="12.2.0"

git clone git@code.onehippo.org:cms-community/hippo-cms-translations.git
git clone git@code.onehippo.org:cms-enterprise/hippo-cms-enterprise-translations.git
git clone git@code.onehippo.org:cms-community/hippo-cms-l10n-tooling.git

modules="hippo-cms-translations hippo-cms-enterprise-translations"

mkdir target

for module in $modules; do
    for locale in $locales; do
	    filename=target/${module}_${release}_${locale}.xlsx
        mvn -N hippo-cms-l10n:export -Dlocale=$locale -f $module/pom.xml
		mv $module/export_$locale.xlsx $filename
    done
done

zip -o -j translation_keys_bloomreach_experience_${release}.zip target/*
