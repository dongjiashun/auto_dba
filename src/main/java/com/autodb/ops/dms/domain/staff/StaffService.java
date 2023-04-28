package com.autodb.ops.dms.domain.staff;

import com.dianwoba.springboot.webapi.WebApiResponse;
import feign.Param;
import feign.RequestLine;
import lombok.Data;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.Serializable;
import java.util.Date;

/**
 * StaffService
 * @author dongjs
 * @since 16/3/18
 */
public interface StaffService {
    @RequestLine("GET /staffs/{userCode}")
    User staffInfo(@Param("userCode") String userCode);

    /** staff service user **/
    @Data
    class User {
        private Integer id; //自生成ID
        private DepartDTO depart; // 部门信息
        private String code; //员工号
        private String name; //名字
        private boolean disable; //是否启用(|停用)
        private String creator; //创建者
        private boolean isLeader;
        private String creattime; //创建时间

        private String duty;
        private String mobile;
        private String email;

        private String thirdpartyAccount;

        private String lastLoginTime; 	//最后登录时间
        private String lastLoginIp;		//最后登录IP
        private String lastLoginCity;	//最后登录城市

        private String previousLoginTime;	//上一次登录时间
        private String previousLoginIp;		//上一次登录IP
        private String previousLoginCity;	//上一次登录城市
    }

    @Data
    class DepartDTO implements Serializable {

        private Integer id;

        private String path;

        private String name;

        private Integer number;

        private Integer parent;

        private Integer cityId;

        private Byte type;

        private String area;

        private String description;

        private String creator;

        private Date createtime;
    }
}
