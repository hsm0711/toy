package com.webapp.controller;

import com.webapp.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@Controller
@RequestMapping("/ip-calculator")
@RequiredArgsConstructor
public class IpCalculatorController {
    
    private final MenuService menuService;
    
    @GetMapping
    public String ipCalculatorPage(Model model) {
        model.addAttribute("menus", menuService.getActiveMenus());
        model.addAttribute("currentPage", "ip-calculator");
        return "ip-calculator";
    }
    
    @PostMapping("/api/calculate")
    @ResponseBody
    public Map<String, Object> calculateCidr(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String cidr = request.get("cidr");
            if (cidr == null || cidr.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "CIDR 표기법을 입력하세요.");
                return result;
            }
            
            String[] parts = cidr.split("/");
            if (parts.length != 2) {
                result.put("success", false);
                result.put("message", "올바른 CIDR 형식이 아닙니다. (예: 192.168.1.0/24)");
                return result;
            }
            
            String ipAddress = parts[0];
            int prefixLength = Integer.parseInt(parts[1]);
            
            if (prefixLength < 0 || prefixLength > 32) {
                result.put("success", false);
                result.put("message", "프리픽스 길이는 0~32 사이여야 합니다.");
                return result;
            }
            
            long ipLong = ipToLong(ipAddress);
            long subnetMask = getSubnetMask(prefixLength);
            long networkAddress = ipLong & subnetMask;
            long broadcastAddress = networkAddress | (~subnetMask & 0xFFFFFFFFL);
            long firstHost = networkAddress + 1;
            long lastHost = broadcastAddress - 1;
            long totalHosts = (long) Math.pow(2, 32 - prefixLength);
            long usableHosts = totalHosts > 2 ? totalHosts - 2 : 0;
            
            result.put("success", true);
            result.put("cidr", cidr);
            result.put("ipAddress", ipAddress);
            result.put("prefixLength", prefixLength);
            result.put("subnetMask", longToIp(subnetMask));
            result.put("wildcardMask", longToIp(~subnetMask & 0xFFFFFFFFL));
            result.put("networkAddress", longToIp(networkAddress));
            result.put("broadcastAddress", longToIp(broadcastAddress));
            result.put("firstHost", prefixLength < 31 ? longToIp(firstHost) : "N/A");
            result.put("lastHost", prefixLength < 31 ? longToIp(lastHost) : "N/A");
            result.put("totalHosts", totalHosts);
            result.put("usableHosts", usableHosts);
            result.put("ipClass", getIpClass(ipAddress));
            result.put("ipType", getIpType(ipAddress));
            
        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "잘못된 숫자 형식입니다.");
        } catch (Exception e) {
            log.error("CIDR 계산 오류", e);
            result.put("success", false);
            result.put("message", "계산 오류: " + e.getMessage());
        }
        
        return result;
    }
    
    private long ipToLong(String ipAddress) {
        String[] octets = ipAddress.split("\\.");
        if (octets.length != 4) {
            throw new IllegalArgumentException("잘못된 IP 주소 형식");
        }
        
        long result = 0;
        for (int i = 0; i < 4; i++) {
            int octet = Integer.parseInt(octets[i]);
            if (octet < 0 || octet > 255) {
                throw new IllegalArgumentException("옥텟 값은 0~255 사이여야 합니다.");
            }
            result = (result << 8) | octet;
        }
        return result;
    }
    
    private String longToIp(long ip) {
        return String.format("%d.%d.%d.%d",
            (ip >> 24) & 0xFF,
            (ip >> 16) & 0xFF,
            (ip >> 8) & 0xFF,
            ip & 0xFF
        );
    }
    
    private long getSubnetMask(int prefixLength) {
        if (prefixLength == 0) {
            return 0L;
        }
        return (0xFFFFFFFFL << (32 - prefixLength)) & 0xFFFFFFFFL;
    }
    
    private String getIpClass(String ipAddress) {
        int firstOctet = Integer.parseInt(ipAddress.split("\\.")[0]);
        
        if (firstOctet >= 1 && firstOctet <= 126) return "A";
        if (firstOctet >= 128 && firstOctet <= 191) return "B";
        if (firstOctet >= 192 && firstOctet <= 223) return "C";
        if (firstOctet >= 224 && firstOctet <= 239) return "D (Multicast)";
        if (firstOctet >= 240 && firstOctet <= 255) return "E (Reserved)";
        
        return "Unknown";
    }
    
    private String getIpType(String ipAddress) {
        String[] octets = ipAddress.split("\\.");
        int first = Integer.parseInt(octets[0]);
        int second = Integer.parseInt(octets[1]);
        
        if (first == 10) return "사설 IP (Private)";
        if (first == 172 && second >= 16 && second <= 31) return "사설 IP (Private)";
        if (first == 192 && second == 168) return "사설 IP (Private)";
        if (first == 127) return "로컬 루프백 (Loopback)";
        if (first == 169 && second == 254) return "링크 로컬 (Link-Local)";
        
        return "공인 IP (Public)";
    }
}
