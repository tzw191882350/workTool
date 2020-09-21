package myFrame.DealStringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 字符串转换工具
 */
public class TransStringUtil {

    /**
     * 执行字符串转换方法.
     * @param type 方法类型
     * @param str 需要转换的字符串
     * @return 转换后的结果
     */
    public static String executeStrTrans(final String type, final String str) {
    	final int typeInt = Integer.parseInt(type);
    	String result = "";
    	switch (typeInt) {
	        case 1:
	        	result = formatPlSqlCopySql(str);
	            break;
	        case 2:
	        	result = buildPoVoTransfer(str);
	            break;
	        case 3:
	        	result = buildInsertSql(str);
	        	break;
	        case 4:
	        	result = mergeMoreLineFlexible(str);
	        	break;
	        default:
	            print("");
	    }
    	return result;
    }
    
    /**
     * 灵活多行合并成一行
     * "合同编号",
     * "合同总金额",
     * "合同签订项目类型",
     * "合同收付款类型",
     * -----------------------------
     * "合同编号","合同总金额","合同签订项目类型","合同收付款类型",
     */
    public static String mergeMoreLineFlexible(final String str) {
    	final String clearStr = clearEmptyLineAndTrim(str);
    	final StringBuilder result = new StringBuilder();
        final String[] strArr = clearStr.split("\n");
        boolean oneToMore = false;
        final int length = strArr.length;
        // 模板
        String modual = "";
        for (int i = 0; i < length; i++) {
        	final String line = strArr[i];
        	if (length == 1) {
        		final String[] moreLine = line.split(",");
        		for (int j = 0; j < moreLine.length; j++) {
					final String string = moreLine[j];
					appendAndLn(result, string);
				}
        		break;
        	}
        	if ((i == 0 && line.contains("{0}"))) {
        		modual = line;
        		oneToMore = true;
        		continue;
        	}
        	if (oneToMore) {
        		final String[] moreLine = line.split(",");
        		for (int j = 0; j < moreLine.length; j++) {
					final String string = moreLine[j];
					appendAndLn(result, modual.replace("{0}", string));
				}
        		
        	} else {
        		result.append(line);
//        		result.append("\\n");
        	}
        }
        return result.toString();
    }

    /**
     * 获取每行某一字符串
     */
    public static String buildInsertSql(final String str) {
        final StringBuilder result = new StringBuilder();
        final StringBuilder errMsg = new StringBuilder();
        final String[] strArr = str.split("\n");
        String val = "";
        for (int i = 0; i < strArr.length; i++) {
        	String line = strArr[i];
			if (i == 0) {
				val = line;
				if ("".equals(val) || val.contains(":")) {
					appendAndLn(result, "第一行请填写你需要需要获取的指定位置标识符");
					appendAndLn(result, "案例如下");
					appendAndLn(result, "第一行：name");
					appendAndLn(result, "第二行：{ \"name\": \"ysxmh\", \"showHint\": true }");
					appendAndLn(result, "输出结果：ysxmh");
					break;
				} else {
					continue;
				}
			}
			line = line.trim();
			if (line != null && !"".equals(line)) {
				if (line.contains(val)) {
					final int datatype = line.indexOf(val);
					int dataColon = line.indexOf(":", datatype);
					if (dataColon == -1 || dataColon > val.length() + datatype + 4) {
						dataColon = line.indexOf("=", datatype);
					}
					if (dataColon == -1 || dataColon > val.length() + datatype + 4) {
						dataColon = line.indexOf("(", datatype);
					}
					final int dataColonQutation = line.indexOf("\"", dataColon);
					final int dataQutationQutation = line.indexOf("\"", dataColonQutation+1);
					final String name = line.substring(dataColonQutation+1, dataQutationQutation);
					result.append(name + ",");
				} else {
					errMsg.append("\n第" + i + "行不包含标识符");
				}
			}
        }
        errMsg.insert(0, result.substring(0, result.length() - 1));
        return errMsg.toString();
    }

    /**
     * 根据VO或者PO生成POVO转换类.
     * po.setSsgkbmbm(vo.getSsgkbmbm());
     * po.setDwxmlx(vo.getDwxmlx());
     * po.setDwxmlb(vo.getDwxmlb());
     */
    public static String buildPoVoTransfer(final String str) {
    	final String[] strArr = str.split("\n");
    	final StringBuilder result = new StringBuilder();
        final List<String> nameList = new ArrayList<String>();
        for (int i = 0; i < strArr.length; i++) {
			final String line = strArr[i];
			if (line.contains("public") && line.contains("get")) {
                final int getIndex = line.indexOf("get");
                final int leftBrakIndex = line.indexOf("(");
                final String name = line.substring(getIndex + 3, leftBrakIndex);
                nameList.add(name);
                print(name + ",");
//				if (getMethod) {
//					print("vo.set"+name+"();");
//				} else {
//					print("vo.set"+name+"(po.get"+name+"());");
//				}
            }
		}
        for (final String name : nameList) {
        	result.append("po.set"+name+"(vo.get"+name+"());\n");
        }
        return result.toString();
    }

    /**
     * 把从plsql复制的sql格式化成java适用的sql
     * 或
     * 把java的sql格式化成plsql适用的sql
     */
    public static String formatPlSqlCopySql(final String str) {
        final StringBuilder result = new StringBuilder();
        final String[] strArr = str.split("\n");
        Boolean tranFlag = true;
        int num = 0;
        final List<String> replace = new ArrayList<String>();
        replace.add("200712");
        replace.add("200713");
        final int len = replace.size();
        for (int i = 0; i < strArr.length; i++) {
			String line = strArr[i];
			if (tranFlag && line.contains("\"")) {
                tranFlag = false;
            }
            if (tranFlag) {
                line = line.replaceAll(";", "");
                if (line.contains("{") && len > 0) {
                    for (int j = 0; j < len; j++) {
                        line = line.replaceAll("\\{" + j + "\\}", replace.get(j));
                    }
                }
                if (num == 0) {
                	result.append("\"").append(line).append("\"\n");
                    num++;
                } else {
                	result.append("+ \" ").append(line).append("\"\n");
                }
            } else {
                line = line.trim();
                line = line.replaceAll("\\+ \"", "");
                line = line.replaceAll("\"", "");
                result.append(line).append("\n");
            }
		}
        return result.toString();
    }
    /**
     * 删除左右空格和空行
     * @return
     */
    public static String clearEmptyLineAndTrim(final String str) {
    	final StringBuilder result = new StringBuilder();
    	if (str != null && !"".equals(str)) {
    		final String[] strArr = str.split("\n");
    		for (int i = 0; i < strArr.length; i++) {
    			final String line = strArr[i].trim();
    			if (!"".equals(line)) {
    				result.append(strArr[i].trim() + "\n");
    			}
    		}
    	}
    	return result.toString();
    }

    /**
     * 根据字段信息自动生成参考模型.
     * @param str
     * @return
     */
    public static String getMdmxByColInfo(final String str) {
    	final String clearStr = clearEmptyLineAndTrim(str);
    	final StringBuilder result = new StringBuilder();
        final String[] strArr = clearStr.split("\n");
        boolean oneToMore = false;
        final int length = strArr.length;
        // 模板
        String modual = "";
        for (int i = 0; i < length; i++) {
        	final String line = strArr[i];
        	if (length == 1) {
        		final String[] moreLine = line.split(",");
        		for (int j = 0; j < moreLine.length; j++) {
					final String string = moreLine[j];
					appendAndLn(result, string);
				}
        		break;
        	}
        	if ((i == 0 && line.contains("{0}"))) {
        		modual = line;
        		oneToMore = true;
        		continue;
        	}
        	if (oneToMore) {
        		final String[] moreLine = line.split(",");
        		for (int j = 0; j < moreLine.length; j++) {
					final String string = moreLine[j];
					appendAndLn(result, modual.replace("{0}", string));
				}
        		
        	} else {
        		result.append(line);
        	}
        }
        return result.toString();
    }
    
    
    
    
    /**
     * 输出
     */
    private static void print(final Object str) {
        System.out.println(str);
    }

    /**
     * append并且换行
     */
    private static void appendAndLn(final StringBuilder sb, final String str) {
    	sb.append(str + "\n");
    }
}
