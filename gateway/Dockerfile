FROM golang:alpine AS builder
WORKDIR /app
COPY gateway/go.mod gateway/go.sum ./
RUN go mod download
COPY gateway/ .
RUN CGO_ENABLED=0 go build -o /gateway .

FROM alpine:3.19
RUN apk --no-cache add ca-certificates
COPY --from=builder /gateway /gateway
EXPOSE 8080
CMD ["/gateway"]
