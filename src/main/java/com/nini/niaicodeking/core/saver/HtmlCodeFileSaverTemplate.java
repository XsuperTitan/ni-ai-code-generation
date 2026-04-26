package com.nini.niaicodeking.core.saver;

import cn.hutool.core.util.StrUtil;
import com.nini.niaicodeking.exception.BusinessException;
import com.nini.niaicodeking.exception.ErrorCode;
import com.nini.niaicodeking.model.HtmlCodeResult;
import com.nini.niaicodeking.model.enums.CodeGenTypeEnum;

/**
 * HTML代码文件保存器
 *
 * @author nini
 */
public class HtmlCodeFileSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.HTML;
    }

    @Override
    protected void saveFiles(HtmlCodeResult result, String baseDirPath) {
        // 保存 HTML 文件
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
    }

    @Override
    protected void validateInput(HtmlCodeResult result) {
        super.validateInput(result);
        // HTML 代码不能为空
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "HTML代码内容不能为空");
        }
    }
}