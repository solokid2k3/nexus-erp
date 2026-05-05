FROM golang:1.23-alpine AS builder
WORKDIR /app
COPY inventory-service/go.mod inventory-service/go.sum ./
RUN go mod download
COPY inventory-service/ .
RUN CGO_ENABLED=0 go build -o /inventory-service .

FROM alpine:3.19
RUN apk --no-cache add ca-certificates
COPY --from=builder /inventory-service /inventory-service
EXPOSE 50051
CMD ["/inventory-service"]
