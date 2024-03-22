package com.fish.vwhub.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 
 * @TableName vw_hub_input
 */
@TableName(value ="vw_hub_input")
@Data
public class VwHubInput implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Object id;

    /**
     * 
     */
    private String fileName;

    /**
     * 
     */
    private Object fileType;

    /**
     * 
     */
    private Date gmtCreate;

    /**
     * 
     */
    private Date finishTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}