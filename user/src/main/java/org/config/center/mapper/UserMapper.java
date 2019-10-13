package org.config.center.mapper;

import org.apache.ibatis.annotations.*;
import org.config.center.core.bean.UserBO;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface UserMapper {
    @Select("select * from `user` where name = :name and password=:password")
    UserBO selectUserByUserId(@Param("name") String name, @Param("password") String password);

    @Insert("insert into `user` (name,pass,email) values(:name,:password,:email)")
    void insertUser(@Param("name") String name, @Param("password") String password,@Param("email") String email);

    @Insert("insert into `permission` (name,key,value) values (:name,:key,:key1)")
    void isnertPermission(@Param("name") String name, @Param("key") String key, @Param("value") String value);

    @Delete("delete from `permission` where name = :name and key=:key and value = :value")
    void deletePermission(@Param("name") String name, @Param("key") String key, @Param("value") String value);
}