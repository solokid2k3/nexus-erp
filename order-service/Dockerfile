FROM golang:1.22-alpine AS builder
WORKDIR /app
COPY order-service/go.mod order-service/go.sum ./
RUN go mod download
COPY order-service/ .
RUN CGO_ENABLED=0 go build -o /order-service .

FROM alpine:3.19
RUN apk --no-cache add ca-certificates
COPY --from=builder /order-service /order-service
EXPOSE 50052
CMD ["/order-service"]
