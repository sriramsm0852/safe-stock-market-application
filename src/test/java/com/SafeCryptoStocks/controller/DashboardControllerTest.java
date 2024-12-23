package com.SafeCryptoStocks.controller;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.SafeCryptoStocks.services.DashboardService;
import com.SafeCryptoStocks.services.MarketService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    @Mock
    private MarketService marketService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @InjectMocks
    private DashboardController dashboardController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testShowMarket_WithValidSession() {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userName")).thenReturn("testUser");
        JsonNode cryptoData = mock(JsonNode.class);
        when(marketService.getTopCryptocurrencies()).thenReturn(cryptoData);

        String viewName = dashboardController.showMarket(model, request);

        assertEquals("market", viewName);
        verify(model).addAttribute("cryptoData", cryptoData);
        verify(model).addAttribute("userName", "testUser");
    }

    @Test
    void testShowMarket_WithInvalidSession() {
        when(request.getSession(false)).thenReturn(null);

        String viewName = dashboardController.showMarket(model, request);

        assertEquals("redirect:/login", viewName);
    }

    @Test
    void testShowBudget_WithValidSession() {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userName")).thenReturn("testUser");
        when(session.getAttribute("firstName")).thenReturn("John");
        when(session.getAttribute("lastName")).thenReturn("Doe");

        String viewName = dashboardController.showBudget(model, request);

        assertEquals("budget", viewName);
        verify(model).addAttribute("userName", "testUser");
        verify(model).addAttribute("firstName", "John");
        verify(model).addAttribute("lastName", "Doe");
    }

    @Test
    void testShowBudget_WithInvalidSession() {
        when(request.getSession(false)).thenReturn(null);

        String viewName = dashboardController.showBudget(model, request);

        assertEquals("redirect:/login", viewName);
    }

    @Test
    void testLearn_WithValidSession() {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userName")).thenReturn("testUser");

        String viewName = dashboardController.learn(model, request);

        assertEquals("learn", viewName);
        verify(model).addAttribute("userName", "testUser");
    }

    @Test
    void testLearn_WithInvalidSession() {
        when(request.getSession(false)).thenReturn(null);

        String viewName = dashboardController.learn(model, request);

        assertEquals("redirect:/login", viewName);
    }

    @Test
    void testShowDashboard() {
        Long userId = 123L;
        String viewName = dashboardController.showDashboard(userId, model);

        assertEquals("dashboard", viewName);
        verify(model).addAttribute("userId", userId);
    }

    @Test
    void testDashboard_WithValidSession() {
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("userName")).thenReturn("testUser");
        when(session.getAttribute("firstName")).thenReturn("John");
        when(session.getAttribute("lastName")).thenReturn("Doe");

        JsonNode cryptoData = mock(JsonNode.class);
        JsonNode trendingData = mock(JsonNode.class);
        JsonNode newsData = mock(JsonNode.class);

        when(dashboardService.getCryptocurrencies()).thenReturn(cryptoData);
        when(dashboardService.getTrendingCryptocurrencies()).thenReturn(trendingData);
        when(dashboardService.getCryptoNews()).thenReturn(newsData);

        String viewName = dashboardController.dashboard(request, model);

        assertEquals("dashboard", viewName);
        verify(model).addAttribute("cryptoData", cryptoData);
        verify(model).addAttribute("trendingData", trendingData);
        verify(model).addAttribute("newsData", newsData);
        verify(model).addAttribute("userName", "testUser");
        verify(model).addAttribute("firstName", "John");
        verify(model).addAttribute("lastName", "Doe");
    }

    @Test
    void testDashboard_WithInvalidSession() {
        when(request.getSession(false)).thenReturn(null);

        String viewName = dashboardController.dashboard(request, model);

        assertEquals("redirect:/login", viewName);
    }
}
