package com.shuangqi.aiagent7.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.shuangqi.aiagent7.model.domain.TPermission;
import com.shuangqi.aiagent7.model.domain.TUser;
import com.shuangqi.aiagent7.model.pojo.AccountUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccountUserDetailsService implements UserDetailsService {
    private final UserService userService;

    public AccountUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        TUser user = userService.getOne(Wrappers.<TUser>lambdaQuery().eq(TUser::getUsername, username), true);
        if (user == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        return new AccountUser(user.getId(), user.getUsername(), user.getPassword(), getUserAuthority(user.getUsername()));
    }

    /**
     * 获取用户权限信息（角色、菜单权限）
     *
     * @param username
     * @return
     */
    public List<GrantedAuthority> getUserAuthority(String username) {
        // 角色(比如ROLE_admin)，菜单操作权限(比如sys:user:list)
        // 角色必须以ROLE_开头，security在判断角色时会自动截取ROLE_
        // 比如ROLE_admin,ROLE_normal,sys:user:list,...

        // 使用；
        // @PreAuthorize配合@EnableGlobalMethodSecurity(prePostEnabled = true)使用
        // @PreAuthorize("hasAuthority('/user/list')")
        // @PreAuthorize("hasAnyRole('admin', 'normal')")
        // @PreAuthorize("hasRole('/user/manager1')") //具有xx权限才支持这个接口
        List<TPermission> permissions = userService.getPermissionByUsername(username);
        String authority = "";
        if (CollectionUtils.isNotEmpty(permissions)) {
            // 当前 url 为权限，权限前加 ROLE_ 开头
            List<String> urls = permissions.stream().map(TPermission::getUrl).collect(Collectors.toList());
            authority = StrUtil.join(",", urls);
        }
        return AuthorityUtils.commaSeparatedStringToAuthorityList(authority);
    }
}