/*
 * Copyright 2013 Alex Lin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opoo.press.slug;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.opoo.press.SlugHelper;

/**
 * @author Alex Lin
 *
 */
public class ChineseToPinyinSlugHelper implements SlugHelper{
	//(c >=19968 && c <= 171941)
	//private static String CHINESE_REGEX = "[\u4e00-\u9fa5]";   
    //private static Pattern CHINESE_PATTERN = Pattern.compile(CHINESE_REGEX); 
	private static HanyuPinyinOutputFormat defaultFormat;
	
	static{
		defaultFormat = new HanyuPinyinOutputFormat(); 
		defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE); 
		defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		defaultFormat.setVCharType(HanyuPinyinVCharType.WITH_V);
	}
	
	@Override
	public String toSlug(String text) {
		if(text == null || text.length() == 0){
			return null;
		}
		text = text.toLowerCase();
		text = text.replace(' ', '-');
		char[] chars = text.toLowerCase().toCharArray();
		StringBuffer sb = new StringBuffer();
		boolean previousIsChinese = false;
		for(char c: chars){
			if((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '-' || c == '_' || c == '/' || c == '.'){
				if(previousIsChinese){
					sb.append('-');
				}
				sb.append(c);
				previousIsChinese = false;
			}else if(isChinese(c)){
				String string = toPinYin(c);
				if(string != null){
					if(sb.length() > 0){
						sb.append("-");
					}
					sb.append(string);
					previousIsChinese = true;
				}
			}
		}
		DefaultSlugHelper.trimDot(sb);
		return sb.toString();
	}
	
	public static String toPinYin(char c){
		try {
			String[] strings = PinyinHelper.toHanyuPinyinStringArray(c, defaultFormat);
			if(strings != null && strings.length > 0){
				return strings[0];
			}
			return null;
		} catch (BadHanyuPinyinOutputFormatCombination e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
			return true;
		}
		return false;
	}
}
