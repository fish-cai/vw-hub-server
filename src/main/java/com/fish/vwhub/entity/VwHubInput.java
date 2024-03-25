package com.fish.vwhub.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

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
    private Integer id;

    /**
     * 
     */
    private String fileName;

    /**
     * 
     */
    private Integer fileType;

    /**
     * 
     */
    private Long gmtCreate;

    /**
     * 
     */
    private Date finishTime;

    @TableField(exist = false)
    private String createTime;

    @TableField(exist = false)
    private String typeName;

    @TableField(exist = false)
    private List<VwHubOutput> results;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}