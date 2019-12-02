/**
 * @ProjectName: 民用软件平台软件
 * @Copyright: 2012 HangZhou Hikvision System Technology Co., Ltd. All Right Reserved.
 * @address: http://www.hikvision.com
 * @date: 2014-6-6 上午8:57:54
 * @Description: 本内容仅限于杭州海康威视数字技术股份有限公司内部使用，禁止转发.
 */
package com.ilab.testysy.cloud;

import com.ilab.testysy.entity.CloudPartInfoFileEx;
import com.videogo.openapi.bean.resp.CloudPartInfoFile;

import java.util.List;

public interface QueryPlayBackListTaskCallback {

    void queryHasNoData();

    void queryOnlyHasLocalFile();

    void queryOnlyLocalNoData();

    void queryLocalException();

    void queryCloudSucess(List<CloudPartInfoFileEx> cloudPartInfoFileEx, int queryMLocalStatus, List<CloudPartInfoFile> cloudPartInfoFile);

    void queryLocalSucess(List<CloudPartInfoFileEx> cloudPartInfoFileEx, int position, List<CloudPartInfoFile> cloudPartInfoFile);

    void queryLocalNoData();

    void queryException();

    void queryTaskOver(int type, int queryMode, int queryErrorCode, String detail);

}
