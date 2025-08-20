package com.ocommerce.services.order.controller;

import com.ocommerce.services.order.dto.CreateOrderRequest;
import com.ocommerce.services.order.dto.OrderResponse;
import com.ocommerce.services.order.mapper.OrderMapper;
import com.ocommerce.services.order.service.OrderService;
import com.ocommerce.services.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@Tag(name = "Order Management", description = "APIs for managing customer orders, including creation, retrieval, and cancellation of orders")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {
    private final OrderService orderService;
    private final OrderMapper orderMapper;

    @PostMapping
    @Operation(
        summary = "Create a new order",
        description = "Creates a new order from the user's current cart contents. The cart must contain at least one item and all items must be available. Requires valid shipping and billing addresses. The cart will be cleared upon successful order creation.",
        operationId = "createOrder"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Order created successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid order request - validation errors or business rule violations"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - valid authentication required",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Cart not found for the authenticated user"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<OrderResponse> createOrder(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(
                description = "Order creation request containing shipping and billing address IDs",
                required = true,
                schema = @Schema(implementation = CreateOrderRequest.class)
            )
            @Valid @RequestBody CreateOrderRequest requestDTO) {
        var order = orderService.createOrderFromCart(
            user.getId(),
            requestDTO.getShippingAddressId(),
            requestDTO.getBillingAddressId()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(orderMapper.toOrderResponse(order));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Retrieve order details",
        description = "Fetches detailed information about a specific order including all order items, addresses, and current status. Only orders belonging to the authenticated user can be accessed.",
        operationId = "getOrderById"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Order details retrieved successfully"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - valid authentication required",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Order not found or not accessible by the authenticated user"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<OrderResponse> getOrder(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(
                description = "Unique identifier of the order",
                required = true,
                example = "550e8400-e29b-41d4-a716-446655440000",
                schema = @Schema(type = "string", format = "uuid")
            )
            @PathVariable UUID id) {
        return orderService.getOrderById(id, user.getId())
            .map(orderMapper::toOrderResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(
        summary = "Retrieve user orders",
        description = "Fetches a paginated list of orders for the authenticated user. Orders are returned in descending order by creation date (most recent first). Supports pagination to handle large order histories efficiently.",
        operationId = "getUserOrders"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Orders retrieved successfully"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - valid authentication required",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<Page<OrderResponse>> getUserOrders(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(
                description = "Page number (0-based)",
                example = "0",
                schema = @Schema(type = "integer", minimum = "0", defaultValue = "0")
            )
            @RequestParam(defaultValue = "0") int page,
            @Parameter(
                description = "Number of orders per page",
                example = "10",
                schema = @Schema(type = "integer", minimum = "1", maximum = "100", defaultValue = "10")
            )
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<OrderResponse> orders = orderService.getUserOrders(user.getId(), pageable)
            .map(orderMapper::toOrderResponse);
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}/cancel")
    @Operation(
        summary = "Cancel an order",
        description = "Cancels an existing order if it's in a cancellable state (PENDING or CONFIRMED). Once cancelled, the order cannot be processed further and any reserved inventory will be released. Orders that are already shipped or delivered cannot be cancelled.",
        operationId = "cancelOrder"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Order cancelled successfully"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Order cannot be cancelled - invalid status"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - valid authentication required",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Order not found or not accessible by the authenticated user"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(mediaType = "application/json")
        )
    })
    public ResponseEntity<OrderResponse> cancelOrder(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(
                description = "Unique identifier of the order to cancel",
                required = true,
                example = "550e8400-e29b-41d4-a716-446655440000",
                schema = @Schema(type = "string", format = "uuid")
            )
            @PathVariable UUID id) {
        var cancelledOrder = orderService.cancelOrder(id, user.getId());
        return ResponseEntity.ok(orderMapper.toOrderResponse(cancelledOrder));
    }
}
