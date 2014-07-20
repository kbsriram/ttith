#!/bin/bash

pwd=`pwd`
src="$1"
dst="$2"

/Applications/Inkscape.app/Contents/Resources/script -z -e "$pwd/../../res/drawable-mdpi/$dst" -w 48 "$pwd/$src"
/Applications/Inkscape.app/Contents/Resources/script -z -e "$pwd/../../res/drawable-hdpi/$dst" -w 72 "$pwd/$src"
/Applications/Inkscape.app/Contents/Resources/script -z -e "$pwd/../../res/drawable-xhdpi/$dst" -w 96 "$pwd/$src"
/Applications/Inkscape.app/Contents/Resources/script -z -e "$pwd/../../res/drawable-xxhdpi/$dst" -w 144 "$pwd/$src"
/Applications/Inkscape.app/Contents/Resources/script -z -e "$pwd/../../res/drawable-xxxhdpi/$dst" -w 192 "$pwd/$src"
